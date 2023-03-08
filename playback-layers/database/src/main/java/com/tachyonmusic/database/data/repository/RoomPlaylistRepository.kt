package com.tachyonmusic.database.data.repository

import android.database.sqlite.SQLiteException
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.R
import com.tachyonmusic.database.data.data_source.PlaylistDao
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toPlaylist
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlaylistRepository(
    private val dao: PlaylistDao,
    private val songRepo: SongRepository,
    private val loopRepo: LoopRepository
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

    override suspend fun setPlaybacksOfPlaylist(
        playlistMediaId: MediaId,
        playbacks: List<MediaId>
    ) {
        dao.setPlaybacks(playlistMediaId, playbacks)
    }

    override suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity? =
        dao.getPlaylistWithMediaId(mediaId)
}