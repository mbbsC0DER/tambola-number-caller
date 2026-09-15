package com.pratham.tambola.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratham.tambola.TambolaApplication
import com.pratham.tambola.data.ThemePreference
import com.pratham.tambola.domain.Session
import com.pratham.tambola.domain.SessionController
import com.pratham.tambola.platform.AndroidSpeechPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TambolaViewModel(private val app: TambolaApplication) : ViewModel() {
    private val speech = AndroidSpeechPlayer(app)
    private val controller = SessionController(app.sessions, speech, viewModelScope)
    val caller = controller.state
    val voiceStatus = speech.status
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    val saved = app.sessions.observeSaved().catch { mutableMessage.value = "Could not load saved sessions. Please reopen the app." }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val theme = app.settings.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)
    val now = MutableStateFlow(System.currentTimeMillis())
    private var maintenance: Job? = null
    private var voiceTest: Job? = null

    init { speech.onInterruption = { pause() } }

    fun foreground(active: Boolean) {
        controller.setForeground(active)
        maintenance?.cancel()
        if (!active) { voiceTest?.cancel(); return }
        maintenance = viewModelScope.launch {
            while (true) {
                now.value = System.currentTimeMillis()
                try { app.sessions.cleanup(caller.value.session?.id) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) { mutableMessage.value = "Could not clean up expired sessions." }
                delay(30_000)
            }
        }
    }

    fun newSession(onReady: () -> Unit) = navigate {
        controller.newSession()
        onReady()
        controller.play()
    }
    fun resume(session: Session, onReady: () -> Unit) = navigate { controller.restore(session.id); onReady() }
    fun quit(onDone: () -> Unit) = navigate { controller.quit(); onDone() }
    fun togglePlayback() = action {
        if (caller.value.isPlaying) controller.pause() else controller.play()
    }
    fun pause() {
        voiceTest?.cancel()
        // Stop audible output synchronously; controller joins the cancelled operation next.
        speech.stop()
        action { controller.pause() }
    }
    fun repeat(called: Boolean) = action { controller.repeat(called) }
    fun setGap(ms: Long) = action { controller.changeGap(ms) }
    fun setTheme(theme: ThemePreference) = action { app.settings.setTheme(theme) }
    fun clearMessage() { mutableMessage.value = null }
    fun reportMessage(message: String) { mutableMessage.value = message }
    fun testVoice() {
        if (voiceTest?.isActive == true) return
        voiceTest = viewModelScope.launch {
            try { speech.prepare(); speech.speak("one three, thirteen") }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { mutableMessage.value = failure.message }
            finally { speech.stop(); speech.releaseFocus() }
        }
    }
    private fun action(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { mutableMessage.value = failure.message ?: "Something went wrong. Please try again." }
    }
    private fun navigate(block: suspend () -> Unit) {
        if (mutableBusy.value) return
        mutableBusy.value = true
        action { try { voiceTest?.cancelAndJoin(); block() } finally { mutableBusy.value = false } }
    }
    override fun onCleared() { controller.close() }

    companion object {
        fun factory(app: TambolaApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TambolaViewModel(app) as T
        }
    }
}
