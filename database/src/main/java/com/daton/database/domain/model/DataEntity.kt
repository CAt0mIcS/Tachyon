package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataEntity(
    var currentPositionInRecentlyPlayedPlaybackMs: Long = 0,
    var recentlyPlayedDurationMs: Long = 0,
    @PrimaryKey val id: Int = 0
)