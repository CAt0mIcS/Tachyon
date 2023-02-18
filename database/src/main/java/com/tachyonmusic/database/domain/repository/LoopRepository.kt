package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.database.domain.model.LoopEntity
import kotlinx.coroutines.flow.Flow
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource

interface LoopRepository {
    suspend fun getLoops(): List<Loop>
    fun getPagedLoops(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Loop>>

    fun observe(): Flow<List<Loop>>

    suspend fun getLoopEntities(): List<LoopEntity>
    suspend fun add(loop: LoopEntity): Resource<Unit>
    suspend fun addAll(loops: List<LoopEntity>): Resource<Unit>
    suspend fun removeIf(pred: (LoopEntity) -> Boolean)

    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Duration): LoopEntity?
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?
}