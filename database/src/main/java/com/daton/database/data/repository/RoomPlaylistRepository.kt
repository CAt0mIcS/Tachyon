package com.daton.database.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.data.repository.shared_action.ConvertEntityToLoop
import com.daton.database.data.repository.shared_action.ConvertEntityToPlaylist
import com.daton.database.data.repository.shared_action.ConvertEntityToSong
import com.daton.database.domain.model.PlaylistEntity
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.PlaylistRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlaylistRepository(
    private val dao: PlaylistDao,
    private val convertEntityToPlaylist: ConvertEntityToPlaylist
) : PlaylistRepository {
    override suspend fun getPlaylists(): List<Playlist> = dao.getPlaylists().map {
        convertEntityToPlaylist(it) ?: TODO("Invalid playlist")
    }

    override fun getPagedPlaylists(
        pageSize: Int,
        prefetchDistance: Int,
        initialLoadSize: Int
    ): Flow<PagingData<Playlist>> {
        val pagingSourceFactory = { dao.getPagedPlaylists() }
        return Pager(
            config = PagingConfig(pageSize, prefetchDistance, initialLoadSize = initialLoadSize),
            // TODO: Mediator (https://farhan-tanvir.medium.com/clean-architecture-in-android-jetpack-compose-paging-3-0-kotlin-mvvm-%E3%83%BCpart-2-8d97cee4dffe)
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { playlistData ->
            playlistData.map { playlist ->
                convertEntityToPlaylist(playlist) ?: TODO("Invalid playlist")
            }
        }
    }

    override suspend fun getPlaylistEntities(): List<PlaylistEntity> = dao.getPlaylists()

    override suspend fun add(playlist: PlaylistEntity) {
        dao.add(playlist)
    }

    override suspend fun addAll(playlists: List<PlaylistEntity>) {
        dao.addAll(playlists)
    }

    override suspend fun removeIf(pred: (PlaylistEntity) -> Boolean) {
        getPlaylistEntities().forEach {
            if (pred(it))
                dao.delete(it)
        }
    }

    override suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity? =
        dao.getPlaylistWithMediaId(mediaId)
}