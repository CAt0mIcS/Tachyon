package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.LoopDao
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.util.toLoop
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLoopRepository(
    private val dao: LoopDao
) : LoopRepository {
    override suspend fun getLoops(): List<Loop> = dao.getLoops().map {
        it.toLoop()
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
                loop.toLoop()
            }
        }
    }

    override fun observe() = dao.observe().map { loops ->
        loops.map {
            it.toLoop()
        }
    }

    override suspend fun getLoopEntities(): List<LoopEntity> = dao.getLoops()

    override suspend fun add(loop: LoopEntity): Resource<Unit> {
        return try {
            dao.add(loop)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun addAll(loops: List<LoopEntity>): Resource<Unit> {
        return try {
            dao.addAll(loops)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
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
        songDuration: Duration
    ): LoopEntity? = dao.findBySong(songTitle, songArtist, songDuration)

    override suspend fun findByMediaId(mediaId: MediaId): LoopEntity? = dao.findByMediaId(mediaId)
}