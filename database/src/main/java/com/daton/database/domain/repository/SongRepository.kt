package com.daton.database.domain.repository

import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAll(): Flow<List<Song>>
}