package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

data class RecentlyPlayed(
    val mediaId: MediaId,
    val position: Duration,
    val duration: Duration,
    val artworkType: String,
    val artworkUrl: String?
)

interface DataRepository {
    suspend fun getData(): DataEntity
    suspend fun setData(data: DataEntity): DataEntity

    fun observe(): Flow<DataEntity>

    suspend fun update(
        recentlyPlayed: RecentlyPlayed? = null,
        repeatMode: RepeatMode? = null,
        spotifyAccessToken: String? = null
    )
}