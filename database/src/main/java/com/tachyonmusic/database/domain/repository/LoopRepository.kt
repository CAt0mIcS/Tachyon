package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.database.domain.model.LoopEntity
import kotlinx.coroutines.flow.Flow

interface LoopRepository {
    suspend fun getLoops(): List<Loop>
    fun getPagedLoops(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Loop>>

    fun observe(): Flow<List<Loop>>

    suspend fun getLoopEntities(): List<LoopEntity>
    suspend fun add(loop: LoopEntity)
    suspend fun addAll(loops: List<LoopEntity>)
    suspend fun removeIf(pred: (LoopEntity) -> Boolean)

    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Long): LoopEntity?
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?
    suspend fun updateArtwork(loop: LoopEntity, artworkType: String, artworkUrl: String? = null)
    suspend fun updateArtworkBySong(
        songMediaId: MediaId,
        artworkType: String,
        artworkUrl: String? = null
    )
}