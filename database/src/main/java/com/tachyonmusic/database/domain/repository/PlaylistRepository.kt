package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun getPlaylists(): List<Playlist>
    fun getPagedPlaylists(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Playlist>>

    fun observe(): Flow<List<Playlist>>

    suspend fun getPlaylistEntities(): List<PlaylistEntity>
    suspend fun add(playlist: PlaylistEntity): Resource<Unit>
    suspend fun addAll(playlists: List<PlaylistEntity>): Resource<Unit>
    suspend fun removeIf(pred: (PlaylistEntity) -> Boolean)
    suspend fun setPlaybacksOfPlaylist(
        playlistMediaId: MediaId,
        playbacks: List<MediaId>
    )

    suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity?
}