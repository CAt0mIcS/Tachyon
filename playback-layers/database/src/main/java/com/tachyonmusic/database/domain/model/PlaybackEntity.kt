package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId
import kotlinx.serialization.Serializable

@Entity
open class PlaybackEntity(
    @PrimaryKey var mediaId: MediaId,
)