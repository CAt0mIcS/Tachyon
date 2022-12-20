package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun getHistoryEntities(): List<HistoryEntity>
    fun getPagedHistory(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Playback>>

    suspend fun getHistory(): List<Playback>
    suspend operator fun plusAssign(playback: PlaybackEntity)
    suspend operator fun minusAssign(playback: PlaybackEntity)
    suspend operator fun minusAssign(playbacks: List<MediaId>)
    suspend fun clear()
}