package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SettingsEntity(
    val ignoreAudioFocus: Boolean = false,
    val autoDownloadAlbumArtwork: Boolean = true,
    val autoDownloadAlbumArtworkWifiOnly: Boolean = true,
    val combineDifferentPlaybackTypes: Boolean = false,
    val songIncDecInterval: Int = 100,
    val audioUpdateInterval: Int = 100,
    val maxPlaybacksInHistory: Int = 25,
    val seekForwardIncrementMs: Long = 10000,
    val seekBackIncrementMs: Long = 10000,
    /**
     * Specifies if milliseconds should be shown in the current position and duration texts above
     * the seek bar in the player
     */
    val shouldMillisecondsBeShown: Boolean = false,
    val excludedSongFiles: List<String> = emptyList(),
    @PrimaryKey val id: Int = 0
)