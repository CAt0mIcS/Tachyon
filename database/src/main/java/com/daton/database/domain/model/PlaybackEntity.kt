package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.playback.Playback

@Entity
open class PlaybackEntity(
    @PrimaryKey var id: Int? = null
)