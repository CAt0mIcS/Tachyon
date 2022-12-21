package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId

@Entity
data class HistoryEntity(
    @PrimaryKey
    var mediaId: MediaId,
    var timestamp: Long = System.currentTimeMillis()
)