package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun observe(): Flow<List<SongEntity>>
    fun observeByMediaId(mediaId: MediaId): Flow<SongEntity>

    suspend fun findByMediaId(mediaId: MediaId): SongEntity?

    suspend fun getSongs(): List<SongEntity>

    suspend fun remove(mediaId: MediaId)
    suspend fun removeIf(pred: (SongEntity) -> Boolean)

    suspend fun addAll(songs: List<SongEntity>): Resource<Unit>
    suspend fun updateArtwork(song: MediaId, artworkType: String, artworkUrl: String? = null)
    suspend fun updateIsHidden(song: MediaId, isHidden: Boolean)
    suspend fun updateMetadata(song: MediaId, title: String, artist: String)
    suspend fun getSongsWithArtworkTypes(vararg artworkTypes: String): List<SongEntity>
}