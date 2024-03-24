package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.CustomizedSongDao
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class RoomCustomizedSongRepository(
    private val dao: CustomizedSongDao
) : CustomizedSongRepository {
    override fun observe() = dao.observe()

    override suspend fun getCustomizedSongs(): List<CustomizedSongEntity> = dao.getCustomizedSongs()

    override suspend fun add(customizedSong: CustomizedSongEntity): Resource<Unit> {
        return try {
            dao.add(customizedSong)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun addAll(customizedSongs: List<CustomizedSongEntity>): Resource<Unit> {
        return try {
            dao.addAll(customizedSongs)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun remove(mediaId: MediaId) {
        dao.delete(mediaId)
    }

    // TODO: Bad performance? Should be changed to have less db queries
    override suspend fun removeIf(pred: (CustomizedSongEntity) -> Boolean) {
        getCustomizedSongs().forEach {
            if (pred(it))
                dao.delete(it.mediaId)
        }
    }

    override suspend fun findBySong(
        songTitle: String,
        songArtist: String,
        songDuration: Duration
    ): CustomizedSongEntity? = dao.findBySong(songTitle, songArtist, songDuration)

    override suspend fun findByMediaId(mediaId: MediaId): CustomizedSongEntity? = dao.findByMediaId(mediaId)

    override suspend fun updateMetadata(song: MediaId, newMediaId: MediaId) {
        dao.updateMetadata(song, newMediaId)
    }
}