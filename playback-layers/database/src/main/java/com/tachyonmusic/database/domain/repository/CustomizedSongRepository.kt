package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface CustomizedSongRepository {
    fun observe(): Flow<List<CustomizedSongEntity>>

    suspend fun getCustomizedSongs(): List<CustomizedSongEntity>
    suspend fun add(customizedSong: CustomizedSongEntity): Resource<Unit>
    suspend fun addAll(customizedSongs: List<CustomizedSongEntity>): Resource<Unit>

    suspend fun remove(mediaId: MediaId)
    suspend fun removeIf(pred: (CustomizedSongEntity) -> Boolean)

    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Duration): CustomizedSongEntity?
    suspend fun findByMediaId(mediaId: MediaId): CustomizedSongEntity?
}