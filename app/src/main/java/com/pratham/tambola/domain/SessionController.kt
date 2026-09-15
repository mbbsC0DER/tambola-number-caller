package com.pratham.tambola.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Main-dispatcher confined. Only transition commands hold the mutex; playback never does. */
class SessionController(
    private val repository: SessionRepository,
    private val speech: SpeechPlayer,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(CallerState())
    val state = mutableState.asStateFlow()
    private val transitions = Mutex()
    private var playback: Job? = null
    private var foreground = false

    fun setForeground(value: Boolean) {
        foreground = value
        if (!value) {
            playback?.cancel()
            speech.stop()
            scope.launch { pause() }
        }
    }

    suspend fun newSession() = transitions.withLock {
        stopPlayback()
        // Commit and publish together even when the screen leaves during the transaction.
        withContext(NonCancellable) {
            repository.cleanup()
            mutableState.value = CallerState(repository.create(), PlaybackMode.PAUSED)
        }
    }

    suspend fun restore(id: String) = transitions.withLock {
        stopPlayback()
        val session = repository.load(id) ?: error("This session has expired or is no longer available.")
        mutableState.value = CallerState(session, PlaybackMode.PAUSED)
    }

    suspend fun play() = transitions.withLock {
        val session = state.value.session ?: return@withLock
        if (!foreground || session.complete || playback?.isActive == true) return@withLock
        mutableState.value = state.value.copy(mode = PlaybackMode.PREPARING, error = null)
        playback = scope.launch {
            runPlayback {
                speech.prepare()
                currentCoroutineContext().ensureActive()
                mutableState.value = state.value.copy(mode = PlaybackMode.CALLING)
                // New games and interrupted utterances start immediately; ordinary resumes wait.
                if (session.drawnCount > 0 && !session.announcementPending) delay(session.gapMs)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    var current = state.value.session ?: return@runPlayback
                    if (current.complete) break
                    if (!current.announcementPending) {
                        withContext(NonCancellable) {
                            current = repository.draw(current.id)
                            mutableState.value = state.value.copy(session = current.copy(gapMs = requireNotNull(state.value.session).gapMs))
                        }
                    }
                    currentCoroutineContext().ensureActive()
                    speech.speak(NumberAnnouncementFormatter.live(requireNotNull(current.current)))
                    currentCoroutineContext().ensureActive()
                    withContext(NonCancellable) {
                        val acknowledged = repository.acknowledge(current.id)
                        mutableState.value = state.value.copy(session = acknowledged.copy(gapMs = requireNotNull(state.value.session).gapMs))
                    }
                    if (state.value.session?.complete == true) break
                    delay(requireNotNull(state.value.session).gapMs)
                }
                mutableState.value = state.value.copy(mode = PlaybackMode.ALL_CALLED)
            }
        }
    }

    suspend fun pause() = transitions.withLock {
        stopPlayback()
        mutableState.value = restingState()
    }

    suspend fun repeat(called: Boolean) = transitions.withLock {
        stopPlayback()
        val session = state.value.session ?: return@withLock
        val numbers = (if (called) session.called else session.remaining).sorted()
        if (numbers.isEmpty() || !foreground) {
            mutableState.value = restingState()
            return@withLock
        }
        mutableState.value = state.value.copy(
            mode = if (called) PlaybackMode.READING_CALLED else PlaybackMode.READING_REMAINING,
            readoutTotal = numbers.size, readoutIndex = 0, readoutNumber = null, error = null,
        )
        playback = scope.launch {
            runPlayback {
                speech.prepare()
                numbers.forEachIndexed { index, number ->
                    currentCoroutineContext().ensureActive()
                    mutableState.value = state.value.copy(readoutNumber = number, readoutIndex = index + 1)
                    speech.speak(NumberAnnouncementFormatter.whole(number))
                    if (index < numbers.lastIndex) delay(REPEAT_GAP_MS)
                }
                mutableState.value = restingState()
            }
        }
    }

    suspend fun changeGap(gapMs: Long) = transitions.withLock {
        require(validGap(gapMs))
        if (state.value.isReading) return@withLock
        val session = state.value.session ?: return@withLock
        // Keep the latest draw/ack state when the independent settings write returns.
        withContext(NonCancellable) {
            if (!session.complete) repository.setGap(session.id, gapMs)
            mutableState.value = state.value.copy(session = state.value.session?.copy(gapMs = gapMs))
        }
    }

    suspend fun quit() = transitions.withLock {
        stopPlayback()
        repository.cleanup()
        mutableState.value = CallerState()
    }

    private fun restingState() = CallerState(
        session = state.value.session,
        mode = when {
            state.value.session == null -> PlaybackMode.IDLE
            state.value.session?.complete == true -> PlaybackMode.ALL_CALLED
            else -> PlaybackMode.PAUSED
        },
    )

    private suspend fun stopPlayback() {
        playback?.cancel()
        speech.stop()
        playback?.cancelAndJoin()
        playback = null
        speech.releaseFocus()
    }

    private suspend fun runPlayback(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            mutableState.value = state.value.copy(
                mode = PlaybackMode.ERROR, readoutNumber = null,
                error = failure.message ?: "Playback stopped. Please try again.",
            )
        } finally {
            speech.stop()
            speech.releaseFocus()
        }
    }

    fun close() { playback?.cancel(); speech.close() }
}
