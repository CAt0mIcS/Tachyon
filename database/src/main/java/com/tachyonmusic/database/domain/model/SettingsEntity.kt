package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.sec

@Entity
data class SettingsEntity(
    val ignoreAudioFocus: Boolean = false,
    val autoDownloadAlbumArtwork: Boolean = true,
    val autoDownloadAlbumArtworkWifiOnly: Boolean = true,
    val combineDifferentPlaybackTypes: Boolean = false,
    val audioUpdateInterval: Duration = 100.ms,
    val maxPlaybacksInHistory: Int = 25,
    val seekForwardIncrement: Duration = 10.sec,
    val seekBackIncrement: Duration = 10.sec,
    val animateText: Boolean = true,
    /**
     * Specifies if milliseconds should be shown in the current position and duration texts above
     * the seek bar in the player
     */
    val shouldMillisecondsBeShown: Boolean = false,
    val excludedSongFiles: List<String> = emptyList(),
    @PrimaryKey val id: Int = 0
)