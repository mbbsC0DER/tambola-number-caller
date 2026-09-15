package com.pratham.tambola.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.pratham.tambola.domain.DEFAULT_GAP_MS
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val expiresAt: Long,
    val gapMs: Long = DEFAULT_GAP_MS,
)

@Entity(
    tableName = "session_numbers",
    primaryKeys = ["sessionId", "position"],
    indices = [Index(value = ["sessionId", "number"], unique = true)],
    foreignKeys = [ForeignKey(entity = SessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)],
)
data class SessionNumberEntity(
    val sessionId: String,
    val position: Int,
    val number: Int,
    val drawnAt: Long? = null,
    val announcementCompletedAt: Long? = null,
)

data class SessionWithNumbers(
    @Embedded val session: SessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId") val numbers: List<SessionNumberEntity>,
)

@Dao
interface SessionDao {
    @Insert suspend fun insertSession(session: SessionEntity)
    @Insert suspend fun insertNumbers(numbers: List<SessionNumberEntity>)
    @Transaction @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<SessionWithNumbers>>
    @Transaction @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun get(id: String): SessionWithNumbers?
    @Query("UPDATE session_numbers SET drawnAt = :time WHERE sessionId = :id AND position = :position AND drawnAt IS NULL")
    suspend fun draw(id: String, position: Int, time: Long): Int
    @Query("UPDATE session_numbers SET announcementCompletedAt = :time WHERE sessionId = :id AND position = :position AND drawnAt IS NOT NULL")
    suspend fun acknowledge(id: String, position: Int, time: Long)
    @Query("UPDATE sessions SET gapMs = :gapMs WHERE id = :id")
    suspend fun setGap(id: String, gapMs: Long)
    @Query("DELETE FROM sessions WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM sessions WHERE expiresAt <= :now AND (:protectedId IS NULL OR id != :protectedId)")
    suspend fun deleteExpired(now: Long, protectedId: String?)
}

@Database(entities = [SessionEntity::class, SessionNumberEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): SessionDao
}
