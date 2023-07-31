package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId

@Entity
class PlaylistEntity(
    val name: String,
    @PrimaryKey val mediaId: MediaId,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,
)