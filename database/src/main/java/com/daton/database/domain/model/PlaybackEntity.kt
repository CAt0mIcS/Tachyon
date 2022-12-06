package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class PlaybackEntity(
    @PrimaryKey var id: Int? = null
)