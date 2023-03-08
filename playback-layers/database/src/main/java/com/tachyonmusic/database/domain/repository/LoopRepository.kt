package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface LoopRepository {
    fun observe(): Flow<List<LoopEntity>>

    suspend fun getLoops(): List<LoopEntity>
    suspend fun add(loop: LoopEntity): Resource<Unit>
    suspend fun addAll(loops: List<LoopEntity>): Resource<Unit>

    suspend fun remove(mediaId: MediaId)
    suspend fun removeIf(pred: (LoopEntity) -> Boolean)

    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Duration): LoopEntity?
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?
}