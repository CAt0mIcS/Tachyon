package com.daton.database.domain.model

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
    val excludedSongFiles: List<String> = emptyList(),
    @PrimaryKey val id: Int? = null
)