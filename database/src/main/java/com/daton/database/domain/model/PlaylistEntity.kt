package com.daton.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId

@Entity
class PlaylistEntity(
    mediaId: MediaId,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,
) : PlaybackEntity(mediaId)