package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.SongDao
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class RoomSongRepository(
    private val dao: SongDao
) : SongRepository {


    override fun observe() = dao.observe()

    override fun observeByMediaId(mediaId: MediaId) = dao.observeByMediaId(mediaId)

    override suspend fun findByMediaId(mediaId: MediaId): SongEntity? =
        dao.getSongWithMediaId(mediaId)

    override suspend fun getSongs(): List<SongEntity> = dao.getSongs()

    override suspend fun remove(mediaId: MediaId) {
        dao.delete(mediaId)
    }

    override suspend fun removeIf(pred: (SongEntity) -> Boolean) {
        val toDelete = mutableListOf<MediaId>()
        getSongs().forEach {
            if (pred(it))
                toDelete += it.mediaId
        }
        dao.deleteAll(toDelete)
    }

    override suspend fun addAll(songs: List<SongEntity>): Resource<Unit> {
        return try {
            dao.addAll(songs)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun updateArtwork(song: MediaId, artworkType: String, artworkUrl: String?) {
        dao.updateArtwork(song, artworkType, artworkUrl)
    }

    override suspend fun updateIsHidden(song: MediaId, isHidden: Boolean) {
        dao.updateIsHidden(song, isHidden)
    }

    override suspend fun updateMetadata(
        song: MediaId,
        title: String,
        artist: String,
        album: String?
    ) {
        dao.updateMetadata(song, title, artist, album)
    }

    override suspend fun getSongsWithArtworkTypes(vararg artworkTypes: String) =
        dao.getSongsWithArtworkTypes(artworkTypes)
}