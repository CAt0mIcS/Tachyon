package com.daton.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId

@Entity
data class PlaylistEntity(
    val name: String,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,
) : PlaybackEntity()