package com.daton.database.domain.repository

import androidx.paging.PagingData
import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun getSongs(): List<Song>
    fun getPagedSongs(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Song>>

    suspend fun findByMediaId(mediaId: MediaId): SongEntity?

    suspend fun getSongEntities(): List<SongEntity>
    suspend fun removeIf(pred: (SongEntity) -> Boolean)
    suspend fun addAll(songs: List<SongEntity>)
    suspend fun updateArtwork(song: SongEntity, artworkType: String, artworkUrl: String? = null)
    suspend fun getSongsWithArtworkTypes(vararg artworkTypes: String): List<SongEntity>
}