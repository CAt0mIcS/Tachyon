package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType

@Entity
class SongEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long,
    artworkType: String = ArtworkType.NO_ARTWORK,
    artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, title, artist, duration, artworkType, artworkUrl)