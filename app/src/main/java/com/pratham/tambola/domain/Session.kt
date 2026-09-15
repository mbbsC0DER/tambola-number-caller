package com.pratham.tambola.domain

const val SESSION_TTL_MS = 15L * 24 * 60 * 60 * 1000
const val DEFAULT_GAP_MS = 2_000L
const val INITIAL_CALL_DELAY_MS = 1_000L
const val REPEAT_GAP_MS = 600L

fun validGap(ms: Long) = ms in 1_000L..6_000L && ms % 500L == 0L

data class Session(
    val id: String,
    val startedAt: Long,
    val expiresAt: Long,
    val order: List<Int>,
    val drawnCount: Int = 0,
    val announcementPending: Boolean = false,
    val gapMs: Long = DEFAULT_GAP_MS,
) {
    init {
        require(order.size == 90 && order.toSet() == (1..90).toSet())
        require(drawnCount in 0..90)
        require(!announcementPending || drawnCount > 0)
        require(validGap(gapMs))
    }
    val called get() = order.take(drawnCount)
    val remaining get() = order.drop(drawnCount)
    val current get() = called.lastOrNull()
    val previous get() = called.dropLast(1).lastOrNull()
    val complete get() = drawnCount == 90 && !announcementPending
}

enum class PlaybackMode { IDLE, PREPARING, PAUSED, CALLING, READING_CALLED, READING_REMAINING, ALL_CALLED, ERROR }

data class CallerState(
    val session: Session? = null,
    val mode: PlaybackMode = PlaybackMode.IDLE,
    val readoutNumber: Int? = null,
    val readoutIndex: Int = 0,
    val readoutTotal: Int = 0,
    val error: String? = null,
) {
    val isReading get() = mode == PlaybackMode.READING_CALLED || mode == PlaybackMode.READING_REMAINING
    val isPlaying get() = mode == PlaybackMode.CALLING || mode == PlaybackMode.PREPARING || isReading
}
