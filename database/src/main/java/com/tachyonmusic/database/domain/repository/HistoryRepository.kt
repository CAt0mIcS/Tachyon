package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun getHistoryEntities(): List<HistoryEntity>
    fun getPagedHistory(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<SinglePlayback>>

    fun observe(): Flow<List<SinglePlayback>>

    suspend fun getHistory(): List<SinglePlayback>
    suspend operator fun plusAssign(playback: SinglePlaybackEntity)

    /**
     * Removes elements containing [mediaId]. If it's a song's media id
     * it will also remove any loops with that song
     */
    suspend fun removeHierarchical(mediaId: MediaId)

    suspend operator fun minusAssign(playback: SinglePlaybackEntity)
    suspend operator fun minusAssign(playbacks: List<MediaId>)

    suspend fun clear()
}