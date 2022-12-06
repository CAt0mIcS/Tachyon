package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class PlaybackEntity(
    @PrimaryKey val id: Int? = null
)