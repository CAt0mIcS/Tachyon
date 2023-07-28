package com.tachyonmusic.database.domain.model

import android.net.Uri
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

    /**
     * The app may revert customization changes to the currently playing media item due to the
     * playlist update that happens when saving a newly created customized song. This controls
     * whether you want to play the newly created customized song or keep playing the old playback
     * which will revert to either no customization (if it's a song) or customization of the customized song
     */
    val playNewlyCreatedCustomizedSong: Boolean = true,

    val excludedSongFiles: List<Uri> = emptyList(),
    val musicDirectories: List<Uri> = emptyList(),
    @PrimaryKey val id: Int = 0
)