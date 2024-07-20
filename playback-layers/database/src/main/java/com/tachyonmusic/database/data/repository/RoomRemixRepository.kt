package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.RemixDao
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class RoomRemixRepository(
    private val dao: RemixDao
) : RemixRepository {
    override fun observe() = dao.observe()

    override suspend fun getRemixes(): List<RemixEntity> = dao.getRemixes()

    override suspend fun add(remix: RemixEntity): Resource<Unit> {
        return try {
            dao.add(remix)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun addAll(remixes: List<RemixEntity>): Resource<Unit> {
        return try {
            dao.addAll(remixes)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun remove(mediaId: MediaId) {
        dao.delete(mediaId)
    }

    // TODO: Bad performance? Should be changed to have less db queries
    override suspend fun removeIf(pred: (RemixEntity) -> Boolean) {
        getRemixes().forEach {
            if (pred(it))
                dao.delete(it.mediaId)
        }
    }

    override suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Duration
    ): RemixEntity? = dao.findBySong(songTitle, songArtist, songDuration)

    override suspend fun findByMediaId(mediaId: MediaId): RemixEntity? = dao.findByMediaId(mediaId)

    override suspend fun updateMetadata(song: MediaId, newMediaId: MediaId) {
        dao.updateMetadata(song, newMediaId)
    }
}