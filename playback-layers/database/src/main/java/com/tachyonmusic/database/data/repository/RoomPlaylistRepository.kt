package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.PlaylistDao
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class RoomPlaylistRepository(
    private val dao: PlaylistDao
) : PlaylistRepository {
    override fun observe() = dao.observe()

    override suspend fun getPlaylists(): List<PlaylistEntity> = dao.getPlaylists()

    override suspend fun add(playlist: PlaylistEntity): Resource<Unit> {
        return try {
            dao.add(playlist)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun addAll(playlists: List<PlaylistEntity>): Resource<Unit> {
        return try {
            dao.addAll(playlists)
            Resource.Success()
        } catch (e: SQLiteException) {
            Resource.Error(UiText.build(e.localizedMessage ?: R.string.database_insert_conflict))
        }
    }

    override suspend fun remove(mediaId: MediaId) {
        dao.delete(mediaId)
    }

    override suspend fun removeIf(pred: (PlaylistEntity) -> Boolean) {
        getPlaylists().forEach {
            if (pred(it))
                dao.delete(it.mediaId)
        }
    }

    override suspend fun hasPlaylist(mediaId: MediaId) = findByMediaId(mediaId) != null

    override suspend fun setPlaybacksOfPlaylist(
        playlistMediaId: MediaId,
        playbacks: List<MediaId>
    ) {
        dao.setPlaybacks(playlistMediaId, playbacks)
    }

    override suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity? =
        dao.getPlaylistWithMediaId(mediaId)
}