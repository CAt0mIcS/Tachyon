package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration

const val SONG_DATABASE_TABLE_NAME = "Songs"

@Entity(tableName = SONG_DATABASE_TABLE_NAME)
class SongEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration,

    // Whether the song should be hidden in the UI
    var isHidden: Boolean = false,
    var artworkType: String = ArtworkType.UNKNOWN,
    var artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, title, artist, duration)