package com.pratham.tambola.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pratham.tambola.domain.SESSION_TTL_MS
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoomSessionRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: RoomSessionRepository
    private var time = 1_000L

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        repository = RoomSessionRepository(db, now = { time }, shuffled = { (1..90).toList() })
    }
    @After fun close() { db.close() }

    @Test fun independentSessionsAndPendingAnnouncementSurviveRepositoryRecreation() = runTest {
        val first = repository.create()
        val second = repository.create()
        repository.draw(first.id)
        val recovered = RoomSessionRepository(db, now = { time }).load(first.id)!!
        assertEquals(1, recovered.drawnCount)
        assertTrue(recovered.announcementPending)
        assertEquals(first.order, recovered.order)
        assertEquals(0, repository.load(second.id)!!.drawnCount)
    }

    @Test fun expiryIsFifteenDaysFromCreationAndCascadesToNumbers() = runTest {
        val first = repository.create()
        time += SESSION_TTL_MS - 1
        assertNotNull(repository.load(first.id))
        time++
        repository.cleanup()
        assertNull(repository.load(first.id))
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM session_numbers").use {
            it.moveToFirst(); assertEquals(0, it.getInt(0))
        }
    }

    @Test fun finalAcknowledgementDeletesStorageButReturnsCompletedSnapshot() = runTest {
        val first = repository.create()
        repeat(89) { repository.draw(first.id); repository.acknowledge(first.id) }
        repository.draw(first.id)
        assertTrue(repository.load(first.id)!!.announcementPending)
        val completed = repository.acknowledge(first.id)
        assertTrue(completed.complete)
        assertEquals(90, completed.called.size)
        assertNull(repository.load(first.id))
    }
}
