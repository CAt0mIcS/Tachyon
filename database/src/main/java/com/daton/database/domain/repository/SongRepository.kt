package com.daton.database.domain.repository

import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.playback.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun removeIf(pred: (Song) -> Boolean)
    suspend fun addAll(songs: List<SongEntity>)
    suspend fun updateArtwork(song: SongEntity, artwork: String?)
}