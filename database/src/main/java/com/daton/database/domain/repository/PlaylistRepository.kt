package com.daton.database.domain.repository

import androidx.paging.PagingData
import com.daton.database.domain.model.PlaylistEntity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun getPlaylists(): List<Playlist>
    fun getPagedPlaylists(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Playlist>>

    suspend fun getPlaylistEntities(): List<PlaylistEntity>
    suspend fun add(playlist: PlaylistEntity)
    suspend fun addAll(playlists: List<PlaylistEntity>)
    suspend fun removeIf(pred: (PlaylistEntity) -> Boolean)

    suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity?
}