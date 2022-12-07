package com.daton.database.domain.repository

import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.playback.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongEntities(): List<SongEntity>
    suspend fun removeIf(pred: (SongEntity) -> Boolean)
    suspend fun addAll(songs: List<SongEntity>)
    suspend fun updateArtwork(song: SongEntity, artworkType: String, artworkUrl: String? = null)
    suspend fun loadArtworks()
}