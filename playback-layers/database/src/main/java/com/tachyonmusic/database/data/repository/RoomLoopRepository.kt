package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.LoopDao
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class RoomLoopRepository(
    private val dao: LoopDao
) : LoopRepository {
    override fun observe() = dao.observe()

    override suspend fun getLoops(): List<LoopEntity> = dao.getLoops()

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

    override suspend fun remove(mediaId: MediaId) {
        dao.delete(mediaId)
    }

    // TODO: Bad performance? Should be changed to have less db queries
    override suspend fun removeIf(pred: (LoopEntity) -> Boolean) {
        getLoops().forEach {
            if (pred(it))
                dao.delete(it.mediaId)
        }
    }

    override suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Duration
    ): LoopEntity? = dao.findBySong(songTitle, songArtist, songDuration)

    override suspend fun findByMediaId(mediaId: MediaId): LoopEntity? = dao.findByMediaId(mediaId)
}