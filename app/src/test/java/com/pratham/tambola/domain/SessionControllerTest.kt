package com.pratham.tambola.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionControllerTest {
    private class MemoryRepository : SessionRepository {
        val sessions = linkedMapOf<String, Session>()
        val changes = MutableStateFlow<List<Session>>(emptyList())
        var nextId = 0
        var drawFailure = false
        var drawGate: CompletableDeferred<Unit>? = null
        private fun save(session: Session): Session {
            if (session.complete) sessions.remove(session.id) else sessions[session.id] = session
            changes.value = sessions.values.toList()
            return session
        }
        override fun observeSaved() = changes
        override suspend fun create(): Session = save(Session((++nextId).toString(), 0, SESSION_TTL_MS,
            listOf(13, 10, 7) + (1..90).filter { it !in listOf(13, 10, 7) }))
        override suspend fun load(id: String) = sessions[id]
        override suspend fun draw(id: String): Session {
            check(!drawFailure) { "Disk full" }
            drawGate?.await()
            val s = sessions.getValue(id)
            return save(s.copy(drawnCount = s.drawnCount + 1, announcementPending = true))
        }
        override suspend fun acknowledge(id: String) = save(sessions.getValue(id).copy(announcementPending = false))
        override suspend fun setGap(id: String, gapMs: Long) { sessions[id]?.let { save(it.copy(gapMs = gapMs)) } }
        override suspend fun cleanup(protectedId: String?) = Unit
    }
    private class FakeSpeech(private val now: () -> Long) : SpeechPlayer {
        val spoken = mutableListOf<Pair<String, Long>>()
        var fail = false
        var stops = 0
        override suspend fun prepare() = Unit
        override suspend fun speak(text: String) {
            spoken += text to now()
            check(!fail) { "Voice failed" }
            delay(100)
        }
        override fun stop() { stops++ }
        override fun releaseFocus() = Unit
        override fun close() = Unit
    }
    private class Fixture(scope: TestScope) {
        val repository = MemoryRepository()
        val speech = FakeSpeech { scope.testScheduler.currentTime }
        val controller = SessionController(repository, speech, scope.backgroundScope)
        init { controller.setForeground(true) }
    }

    @Test fun `all ninety are unique and completion stays on screen without a saved record`() = runTest {
        val f = Fixture(this)
        f.controller.newSession()
        f.controller.play()
        advanceTimeBy(200_000); runCurrent()
        val state = f.controller.state.value
        assertEquals(PlaybackMode.ALL_CALLED, state.mode)
        assertEquals((1..90).toSet(), state.session!!.called.toSet())
        assertEquals(90, f.speech.spoken.size)
        assertTrue(f.repository.sessions.isEmpty())
        f.controller.play(); runCurrent()
        assertEquals(90, f.speech.spoken.size)
        f.controller.repeat(true); advanceTimeBy(101); runCurrent()
        assertTrue(f.controller.state.value.isReading)
        f.controller.quit()
        assertNull(f.controller.state.value.session)
    }

    @Test fun `repeated play commands cannot launch overlapping loops`() = runTest {
        val f = Fixture(this)
        f.controller.newSession()
        repeat(20) { f.controller.play() }
        advanceTimeBy(1_101); runCurrent()
        assertEquals(listOf("one three, thirteen"), f.speech.spoken.map { it.first })
        assertEquals(1, f.controller.state.value.session!!.drawnCount)
    }

    @Test fun `interrupted speech is repeated without drawing twice`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play()
        advanceTimeBy(1_050); runCurrent()
        f.controller.pause()
        val pending = f.controller.state.value.session!!
        assertTrue(pending.announcementPending)
        f.controller.play(); advanceTimeBy(101); runCurrent()
        assertEquals(1, f.controller.state.value.session!!.drawnCount)
        assertFalse(f.controller.state.value.session!!.announcementPending)
        assertEquals(listOf("one three, thirteen", "one three, thirteen"), f.speech.spoken.map { it.first })
    }

    @Test fun `read remaining uses whole words and fixed 600ms gaps without drawing`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.changeGap(6_000)
        val before = f.controller.state.value.session
        f.controller.repeat(false)
        advanceTimeBy(1_401); runCurrent(); f.controller.pause()
        assertEquals(before, f.controller.state.value.session)
        assertEquals(listOf("one", "two", "three"), f.speech.spoken.map { it.first })
        assertEquals(listOf(0L, 700L, 1_400L), f.speech.spoken.map { it.second })
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
    }

    @Test fun `repeat called sorts its snapshot and stays paused after finishing`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play()
        advanceTimeBy(5_301); runCurrent(); f.controller.pause()
        val before = f.controller.state.value.session
        f.controller.repeat(true)
        advanceTimeBy(1_501); runCurrent()
        assertEquals(listOf("seven", "ten", "thirteen"), f.speech.spoken.takeLast(3).map { it.first })
        assertEquals(before, f.controller.state.value.session)
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
    }

    @Test fun `speed changes only the next gap`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play()
        advanceTimeBy(1_200); runCurrent()
        f.controller.changeGap(6_000)
        advanceTimeBy(8_001); runCurrent()
        assertEquals(listOf(1_000L, 3_100L, 9_200L), f.speech.spoken.map { it.second })
    }

    @Test fun `new games coexist with quit games and retain pending recovery`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play(); advanceTimeBy(1_050); runCurrent()
        val firstId = f.controller.state.value.session!!.id
        f.controller.newSession()
        assertEquals(2, f.repository.sessions.size)
        f.controller.quit()
        f.controller.restore(firstId)
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
        f.controller.play(); advanceTimeBy(101); runCurrent()
        assertEquals(1, f.controller.state.value.session!!.drawnCount)
        assertFalse(f.controller.state.value.session!!.announcementPending)
    }

    @Test fun `backgrounding stops speech and never resumes automatically`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play(); advanceTimeBy(1_050); runCurrent()
        f.controller.setForeground(false); runCurrent()
        advanceTimeBy(60_000); runCurrent()
        assertEquals(1, f.speech.spoken.size)
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
        f.controller.setForeground(true); advanceTimeBy(10_000); runCurrent()
        assertEquals(1, f.speech.spoken.size)
    }

    @Test fun `speech errors preserve a pending number for retry`() = runTest {
        val f = Fixture(this)
        f.speech.fail = true
        f.controller.newSession(); f.controller.play(); advanceTimeBy(1_000); runCurrent()
        assertEquals(PlaybackMode.ERROR, f.controller.state.value.mode)
        assertTrue(f.controller.state.value.session!!.announcementPending)
        f.speech.fail = false
        f.controller.play(); advanceTimeBy(101); runCurrent()
        assertEquals(1, f.controller.state.value.session!!.drawnCount)
        assertFalse(f.controller.state.value.session!!.announcementPending)
    }

    @Test fun `failed persistence never announces or advances a number`() = runTest {
        val f = Fixture(this)
        f.repository.drawFailure = true
        f.controller.newSession(); f.controller.play(); advanceTimeBy(1_000); runCurrent()
        assertEquals(PlaybackMode.ERROR, f.controller.state.value.mode)
        assertEquals(0, f.controller.state.value.session!!.drawnCount)
        assertTrue(f.speech.spoken.isEmpty())
    }

    @Test fun `pause during a database commit preserves the pending draw without speaking`() = runTest {
        val f = Fixture(this)
        val commit = CompletableDeferred<Unit>()
        f.repository.drawGate = commit
        f.controller.newSession(); f.controller.play(); advanceTimeBy(1_000); runCurrent()
        val pause = backgroundScope.launch { f.controller.pause() }
        runCurrent()
        commit.complete(Unit)
        runCurrent(); pause.join()
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
        assertEquals(1, f.controller.state.value.session!!.drawnCount)
        assertTrue(f.controller.state.value.session!!.announcementPending)
        assertTrue(f.speech.spoken.isEmpty())
    }

    @Test fun `switching readout modes stops the old snapshot immediately`() = runTest {
        val f = Fixture(this)
        f.controller.newSession(); f.controller.play()
        advanceTimeBy(1_101); runCurrent(); f.controller.pause()
        val before = f.controller.state.value.session
        f.controller.repeat(false); advanceTimeBy(50); runCurrent()
        f.controller.repeat(true); advanceTimeBy(101); runCurrent()
        assertEquals("thirteen", f.speech.spoken.last().first)
        assertEquals(before, f.controller.state.value.session)
        assertEquals(PlaybackMode.PAUSED, f.controller.state.value.mode)
    }

    @Test fun `each new session waits one second before its first announcement`() = runTest {
        val f = Fixture(this)
        repeat(2) {
            f.controller.newSession()
            val startedAt = testScheduler.currentTime
            val previousCalls = f.speech.spoken.size
            f.controller.play()
            advanceTimeBy(999); runCurrent()
            assertEquals(previousCalls, f.speech.spoken.size)
            assertEquals(0, f.controller.state.value.session!!.drawnCount)
            advanceTimeBy(1); runCurrent()
            assertEquals(startedAt + 1_000L, f.speech.spoken.last().second)
            assertEquals(previousCalls + 1, f.speech.spoken.size)
        }
    }
}
