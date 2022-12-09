package com.daton.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.daton.database.data.data_source.HistoryDao
import com.daton.database.data.repository.shared_action.ConvertEntityToPlayback
import com.daton.database.data.repository.shared_action.FindPlaybackByMediaId
import com.daton.database.data.repository.shared_action.UpdateArtwork
import com.daton.database.domain.model.*
import com.daton.database.domain.repository.HistoryRepository
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val dao: HistoryDao,
    private val convertEntityToPlayback: ConvertEntityToPlayback,
    private val findPlaybackByMediaId: FindPlaybackByMediaId
) : HistoryRepository {

    override suspend fun getHistoryEntities(): List<PlaybackEntity> =
        dao.getHistory().map { findPlaybackByMediaId(it.mediaId)!! }

    override fun getPagedHistory(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<Playback>> {
        val pagingSourceFactory = { dao.getPagedHistory() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { historyData ->
            historyData.map { playback ->
                convertEntityToPlayback(findPlaybackByMediaId(playback.mediaId)!!)
            }
        }
    }

    override suspend fun getHistory(): List<Playback> =
        dao.getHistory().map { convertEntityToPlayback(findPlaybackByMediaId(it.mediaId)!!) }

    override suspend fun plusAssign(playback: PlaybackEntity) {
        dao.addHistory(HistoryEntity(playback.mediaId))
    }

    override suspend fun minusAssign(playback: PlaybackEntity) {
        dao.removeHistory(playback.mediaId)
    }

    override suspend fun minusAssign(playbacks: List<PlaybackEntity>) {
        dao.removeHistory(playbacks.map { it.mediaId })
    }

    override suspend fun clear() {
        dao.clear()
    }
}