package com.tachyonmusic.database.domain.repository

import androidx.paging.PagingData
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.SongEntity
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun getSongs(): List<Song>
    fun getPagedSongs(
        pageSize: Int,
        prefetchDistance: Int = pageSize,
        initialLoadSize: Int = pageSize
    ): Flow<PagingData<Song>>

    fun observe(): Flow<List<Song>>

    suspend fun findByMediaId(mediaId: MediaId): SongEntity?

    suspend fun getSongEntities(): List<SongEntity>
    suspend fun removeIf(pred: (SongEntity) -> Boolean)
    suspend fun addAll(songs: List<SongEntity>)
    suspend fun updateArtwork(song: SongEntity, artworkType: String, artworkUrl: String? = null)
    suspend fun getSongsWithArtworkTypes(vararg artworkTypes: String): List<SongEntity>
}