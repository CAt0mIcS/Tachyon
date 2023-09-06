package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

const val DATA_DATABASE_TABLE_NAME = "Data"

@Entity(tableName = DATA_DATABASE_TABLE_NAME)
data class DataEntity(
    var recentlyPlayedMediaId: MediaId? = null,
    var currentPositionInRecentlyPlayedPlayback: Duration = 0.ms,
    var recentlyPlayedDuration: Duration = 0.ms,
    var recentlyPlayedArtworkType: String = ArtworkType.UNKNOWN,
    var recentlyPlayedArtworkUrl: String? = null,

    var repeatMode: RepeatMode = RepeatMode.All,

    @PrimaryKey val id: Int = 0
)