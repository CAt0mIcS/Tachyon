package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration

@Entity
data class SongEntity(
    @PrimaryKey val mediaId: MediaId,
    val title: String,
    val artist: String,
    val duration: Duration,

    // Whether the song should be hidden in the UI
    val isHidden: Boolean = false,

    // Spotify songs may be unavailable, not used for local songs
    val isPlayable: Boolean = true,
    var artworkType: String = ArtworkType.UNKNOWN,
    var artworkUrl: String? = null
)