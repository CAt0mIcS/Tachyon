package com.tachyonmusic.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.data.data_source.HistoryDao
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.util.toPlayback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHistoryRepository(
    private val dao: HistoryDao,
    private val findPlaybackByMediaId: FindPlaybackByMediaId,
    private val songRepo: SongRepository,
    private val loopRepo: LoopRepository
) : HistoryRepository {

    override suspend fun getHistoryEntities(): List<HistoryEntity> = dao.getHistory()

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
                findPlaybackByMediaId(playback.mediaId)?.toPlayback(songRepo, loopRepo)
                    ?: TODO("Playback not found ${playback.mediaId}")
            }
        }
    }

    override fun observe() = dao.observe().map { history ->
        history.map {
            // TODO: Slow!!!
            findPlaybackByMediaId(it.mediaId)?.toPlayback(songRepo, loopRepo)
                ?: TODO("Playback not found ${it.mediaId}")
        }
    }

    override suspend fun getHistory(): List<Playback> =
        dao.getHistory().map {
            findPlaybackByMediaId(it.mediaId)?.toPlayback(songRepo, loopRepo)
                ?: TODO("Playback not found ${it.mediaId}")
        }

    override suspend fun plusAssign(playback: PlaybackEntity) {
        dao.addHistory(HistoryEntity(playback.mediaId))
    }

    override suspend fun minusAssign(playback: PlaybackEntity) {
        dao.removeHistory(playback.mediaId)
    }

    override suspend fun minusAssign(playbacks: List<MediaId>) {
        dao.removeHistory(playbacks)
    }

    override suspend fun clear() {
        dao.clear()
    }
}