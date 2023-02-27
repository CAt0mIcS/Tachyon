package com.tachyonmusic.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.data.data_source.HistoryDao
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.use_case.ConvertHistoryEntityToPlayback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHistoryRepository(
    private val dao: HistoryDao,
    private val entityToPlayback: ConvertHistoryEntityToPlayback
) : HistoryRepository {

    override suspend fun getHistoryEntities(): List<HistoryEntity> = dao.getHistory()

    override fun getPagedHistory(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<SinglePlayback>> {
        val pagingSourceFactory = { dao.getPagedHistory() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { historyData ->
            historyData.map { playback ->
                entityToPlayback(playback) ?: TODO("Playback not found ${playback.mediaId}")
            }
        }
    }

    override fun observe() = dao.observe().map { history ->
        history.map {
            // TODO: Slow!!!
            entityToPlayback(it) ?: TODO("Playback not found ${it.mediaId}")
        }
    }

    override suspend fun getHistory(): List<SinglePlayback> =
        dao.getHistory().map {
            entityToPlayback(it) ?: TODO("Playback not found ${it.mediaId}")
        }

    override suspend fun plusAssign(playback: SinglePlaybackEntity) {
        dao.addHistory(HistoryEntity(playback.mediaId))
    }

    override suspend fun minusAssign(playback: SinglePlaybackEntity) {
        dao.removeHistory(playback.mediaId)
    }

    override suspend fun minusAssign(playbacks: List<MediaId>) {
        dao.removeHistory(playbacks)
    }

    override suspend fun clear() {
        dao.clear()
    }
}