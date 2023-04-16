package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId

@Entity
open class PlaybackEntity(
    @PrimaryKey var mediaId: MediaId,
)