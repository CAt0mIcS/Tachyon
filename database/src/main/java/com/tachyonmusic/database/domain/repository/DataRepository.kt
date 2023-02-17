package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.util.Duration

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

    suspend fun updateRecentlyPlayed(recentlyPlayed: RecentlyPlayed)
}