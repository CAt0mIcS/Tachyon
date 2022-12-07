package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import com.tachyonmusic.core.domain.MediaId

@Entity
class SongEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Long,
) : SinglePlaybackEntity(mediaId, title, artist, duration)