package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observe(): Flow<List<PlaylistEntity>>

    suspend fun getPlaylists(): List<PlaylistEntity>
    suspend fun add(playlist: PlaylistEntity): Resource<Unit>
    suspend fun addAll(playlists: List<PlaylistEntity>): Resource<Unit>

    suspend fun remove(mediaId: MediaId)
    suspend fun removeIf(pred: (PlaylistEntity) -> Boolean)

    suspend fun setPlaybacksOfPlaylist(
        playlistMediaId: MediaId,
        playbacks: List<MediaId>
    )

    suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity?
}