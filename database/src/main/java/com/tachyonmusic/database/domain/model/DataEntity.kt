package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType

@Entity
data class DataEntity(
    var recentlyPlayedMediaId: MediaId? = null,
    var currentPositionInRecentlyPlayedPlaybackMs: Long = 0,
    var recentlyPlayedDurationMs: Long = 0,
    var recentlyPlayedArtworkType: String = ArtworkType.UNKNOWN,
    var recentlyPlayedArtworkUrl: String? = null,
    @PrimaryKey val id: Int = 0
)