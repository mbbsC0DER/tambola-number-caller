package com.pratham.tambola.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import com.pratham.tambola.domain.SpeechPlayer
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout

/** All state is confined to the main thread, including marshalled TTS callbacks. */
class AndroidSpeechPlayer(context: Context) : SpeechPlayer {
    private val context = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audio = this.context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(attributes).setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener({ change ->
            if (change != AudioManager.AUDIOFOCUS_GAIN && ownsFocus) onInterruption()
        }, handler).build()
    var onInterruption: () -> Unit = {}
    private var ownsFocus = false
    private var tts: TextToSpeech? = null
    private var initialization: CompletableDeferred<Unit>? = null
    private var pending: Pair<String, CompletableDeferred<Unit>>? = null
    private var closed = false
    private val mutableStatus = MutableStateFlow("An installed English voice is required")
    val status = mutableStatus.asStateFlow()

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY && ownsFocus) onInterruption()
        }
    }

    init {
        ContextCompat.registerReceiver(this.context, noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_EXPORTED)
    }

    override suspend fun prepare() {
        check(!closed) { "Voice player is closed." }
        if (initialization == null || initialization?.isCancelled == true) initialize()
        try {
            withTimeout(15_000) { requireNotNull(initialization).await() }
        } catch (timeout: TimeoutCancellationException) {
            initialization?.cancel()
            mutableStatus.value = "Voice setup timed out. Check your speech settings."
            error(mutableStatus.value)
        }
        if (!ownsFocus) {
            check(audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                "Audio is busy. Return to Tambola and press Play to try again."
            }
            ownsFocus = true
        }
    }

    private fun initialize() {
        tts?.shutdown()
        val ready = CompletableDeferred<Unit>()
        initialization = ready
        mutableStatus.value = "Checking installed voices…"
        tts = TextToSpeech(context) { result ->
            handler.post {
                if (closed || initialization !== ready) return@post
                try {
                    check(result == TextToSpeech.SUCCESS) { "Install or enable a text-to-speech engine in system settings." }
                    val engine = requireNotNull(tts)
                    val voice = engine.voices.orEmpty()
                        .filter { it.locale.language == "en" && !it.isNetworkConnectionRequired &&
                            TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
                        .sortedWith(compareByDescending<android.speech.tts.Voice> { it.locale.country == "IN" }
                            .thenByDescending { it.quality })
                        .firstOrNull()
                    check(voice != null) { "Download an offline English voice in system speech settings, then try again." }
                    check(engine.setVoice(voice) == TextToSpeech.SUCCESS) { "This voice is unavailable. Try another installed English voice." }
                    engine.setSpeechRate(1f)
                    engine.setAudioAttributes(attributes)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit
                        override fun onDone(utteranceId: String?) = finish(utteranceId, null)
                        @Deprecated("Required by the Android TTS interface")
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onError(utteranceId: String?) = finish(utteranceId, "Voice playback failed. Press Play to retry.")
                        override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId, "Voice playback failed ($errorCode). Check your installed voice.")
                        override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId, "Announcement interrupted. Press Play to repeat it.")
                    })
                    mutableStatus.value = "${voice.locale.displayName} · Offline voice ready"
                    ready.complete(Unit)
                } catch (failure: Exception) {
                    mutableStatus.value = failure.message ?: "Voice unavailable"
                    ready.completeExceptionally(failure)
                }
            }
        }
    }

    private fun finish(id: String?, failure: String?) {
        handler.post {
            pending?.takeIf { it.first == id }?.second?.let {
                if (failure == null) it.complete(Unit) else it.completeExceptionally(IllegalStateException(failure))
            }
        }
    }

    override suspend fun speak(text: String) {
        check(ownsFocus) { "Audio focus was lost. Press Play to continue." }
        check(pending == null) { "Another announcement is still playing." }
        val id = UUID.randomUUID().toString()
        val completion = CompletableDeferred<Unit>()
        pending = id to completion
        try {
            check(tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS) {
                "Unable to announce this number. Check your speech settings."
            }
            try {
                withTimeout(30_000) { completion.await() }
            } catch (timeout: TimeoutCancellationException) {
                error("The voice engine stopped responding. Press Play to retry.")
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // Recheck installed voices on retry after an engine failure or voice removal.
            initialization = null
            mutableStatus.value = failure.message ?: "Voice unavailable"
            throw failure
        } finally {
            if (pending?.first == id) { pending = null; tts?.stop() }
            completion.cancel()
        }
    }

    override fun stop() {
        pending?.second?.cancel()
        pending = null
        tts?.stop()
    }

    override fun releaseFocus() {
        if (ownsFocus) { ownsFocus = false; audio.abandonAudioFocusRequest(focus) }
    }

    override fun close() {
        if (closed) return
        closed = true
        stop()
        releaseFocus()
        initialization?.cancel()
        tts?.shutdown()
        context.unregisterReceiver(noisyReceiver)
    }
}
