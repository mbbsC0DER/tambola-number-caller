package com.pratham.tambola.domain

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSaved(): Flow<List<Session>>
    suspend fun create(): Session
    suspend fun load(id: String): Session?
    suspend fun draw(id: String): Session
    suspend fun acknowledge(id: String): Session
    suspend fun setGap(id: String, gapMs: Long)
    suspend fun cleanup(protectedId: String? = null)
}

interface SpeechPlayer {
    suspend fun prepare()
    suspend fun speak(text: String)
    fun stop()
    fun releaseFocus()
    fun close()
}
