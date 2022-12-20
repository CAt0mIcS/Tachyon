package com.daton.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.daton.database.data.data_source.LoopDao
import com.daton.database.data.repository.shared_action.ConvertEntityToLoop
import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.repository.LoopRepository
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLoopRepository(
    private val dao: LoopDao,
    private val convertEntityToLoop: ConvertEntityToLoop
) : LoopRepository {
    override suspend fun getLoops(): List<Loop> = dao.getLoops().map {
        RemoteLoopImpl.build(
            it.mediaId,
            TimingDataController(it.timingData, it.currentTimingDataIndex),
            it.songTitle,
            it.songArtist,
            it.songDuration
        ) ?: TODO("Invalid remote loop media id ${it.mediaId}")
    }

    override fun getPagedLoops(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<Loop>> {
        val pagingSourceFactory = { dao.getPagedLoops() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { loopData ->
            loopData.map { loop ->
                convertEntityToLoop(loop)
            }
        }
    }

    override suspend fun getLoopEntities(): List<LoopEntity> = dao.getLoops()

    override suspend fun add(loop: LoopEntity) {
        dao.add(loop)
    }

    override suspend fun addAll(loops: List<LoopEntity>) {
        dao.addAll(loops)
    }

    // TODO: Bad performance? Should be changed to have less db queries
    override suspend fun removeIf(pred: (LoopEntity) -> Boolean) {
        getLoopEntities().forEach {
            if (pred(it))
                dao.delete(it)
        }
    }

    override suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Long
    ): LoopEntity? = dao.findBySong(songTitle, songArtist, songDuration)

    override suspend fun findByMediaId(mediaId: MediaId): LoopEntity? = dao.findByMediaId(mediaId)

    override suspend fun updateArtwork(loop: LoopEntity, artworkType: String, artworkUrl: String?) {
        dao.updateArtwork(loop.id ?: return, artworkType, artworkUrl)
    }
}