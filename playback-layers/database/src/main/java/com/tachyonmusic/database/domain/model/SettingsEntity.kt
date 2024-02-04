package com.tachyonmusic.database.domain.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.sec

const val SETTINGS_DATABASE_TABLE_NAME = "Settings"

@Entity(tableName = SETTINGS_DATABASE_TABLE_NAME)
data class SettingsEntity(
    var ignoreAudioFocus: Boolean = false,
    var autoDownloadAlbumArtwork: Boolean = true,
    var autoDownloadAlbumArtworkWifiOnly: Boolean = true,
    var combineDifferentPlaybackTypes: Boolean = false,
    var audioUpdateInterval: Duration = 100.ms,
    var maxPlaybacksInHistory: Int = 25,
    var seekForwardIncrement: Duration = 10.sec,
    var seekBackIncrement: Duration = 10.sec,
    var animateText: Boolean = true,
    /**
     * Specifies if milliseconds should be shown in the current position and duration texts above
     * the seek bar in the player
     */
    var shouldMillisecondsBeShown: Boolean = false,

    /**
     * The app may revert customization changes to the currently playing media item due to the
     * playlist update that happens when saving a newly created customized song. This controls
     * whether you want to play the newly created customized song or keep playing the old playback
     * which will revert to either no customization (if it's a song) or customization of the customized song
     */
    var playNewlyCreatedCustomizedSong: Boolean = true,

    var excludedSongFiles: List<Uri> = emptyList(),
    var musicDirectories: List<Uri> = emptyList(),
    @PrimaryKey var id: Int = 0
)