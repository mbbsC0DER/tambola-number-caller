package com.pratham.tambola.data

import androidx.room.withTransaction
import com.pratham.tambola.domain.SESSION_TTL_MS
import com.pratham.tambola.domain.Session
import com.pratham.tambola.domain.SessionRepository
import com.pratham.tambola.domain.validGap
import java.util.UUID
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val database: AppDatabase,
    private val now: () -> Long = System::currentTimeMillis,
    private val shuffled: () -> List<Int> = { (1..90).shuffled() },
) : SessionRepository {
    private val dao = database.sessions()

    override fun observeSaved() = dao.observeSessions().map { rows ->
        rows.map { it.toDomain() }.filter { it.expiresAt > now() && !it.complete }
    }

    override suspend fun create(): Session = database.withTransaction {
        val timestamp = now()
        val session = Session(UUID.randomUUID().toString(), timestamp, timestamp + SESSION_TTL_MS, shuffled())
        dao.insertSession(SessionEntity(session.id, session.startedAt, session.expiresAt))
        dao.insertNumbers(session.order.mapIndexed { index, number -> SessionNumberEntity(session.id, index + 1, number) })
        session
    }

    override suspend fun load(id: String): Session? = database.withTransaction {
        val session = dao.get(id)?.toDomain() ?: return@withTransaction null
        if (session.expiresAt <= now() || session.complete) { dao.delete(id); null } else session
    }

    override suspend fun draw(id: String): Session = database.withTransaction {
        val session = requireSession(id)
        check(!session.complete) { "All numbers have already been called." }
        if (!session.announcementPending) {
            check(dao.draw(id, session.drawnCount + 1, now()) == 1)
        }
        requireSession(id)
    }

    override suspend fun acknowledge(id: String): Session = database.withTransaction {
        val session = requireSession(id)
        check(session.announcementPending)
        dao.acknowledge(id, session.drawnCount, now())
        val updated = requireSession(id)
        // The caller retains this snapshot in memory until the host leaves.
        if (updated.complete) dao.delete(id)
        updated
    }

    override suspend fun setGap(id: String, gapMs: Long) = database.withTransaction {
        require(validGap(gapMs))
        dao.setGap(id, gapMs)
    }

    override suspend fun cleanup(protectedId: String?) { dao.deleteExpired(now(), protectedId) }

    private suspend fun requireSession(id: String) = checkNotNull(dao.get(id)) { "Session unavailable." }.toDomain()

    private fun SessionWithNumbers.toDomain(): Session {
        val ordered = numbers.sortedBy { it.position }
        val drawn = ordered.filter { it.drawnAt != null }
        check(ordered.take(drawn.size) == drawn) { "Invalid session sequence." }
        check(drawn.dropLast(1).all { it.announcementCompletedAt != null }) { "Invalid announcement progress." }
        return Session(
            session.id, session.startedAt, session.expiresAt, ordered.map { it.number }, drawn.size,
            drawn.lastOrNull()?.let { it.announcementCompletedAt == null } ?: false, session.gapMs,
        )
    }
}
