package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration

@Entity
class SongEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration,
    var artworkType: String = ArtworkType.UNKNOWN,
    var artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, title, artist, duration)