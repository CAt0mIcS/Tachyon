package com.daton.database.domain.repository

import com.daton.database.domain.model.DataEntity

data class RecentlyPlayed(
    val positionMs: Long,
    val durationMs: Long
)

interface DataRepository {
    suspend fun getData(): DataEntity
    suspend fun setData(data: DataEntity): DataEntity

    suspend fun updateRecentlyPlayed(recentlyPlayed: RecentlyPlayed)
}