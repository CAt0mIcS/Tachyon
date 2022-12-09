package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import com.daton.database.domain.ArtworkType
import com.tachyonmusic.core.domain.MediaId

@Entity
class SongEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long,
    artworkType: String = ArtworkType.NO_ARTWORK,
    artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, title, artist, duration, artworkType, artworkUrl)