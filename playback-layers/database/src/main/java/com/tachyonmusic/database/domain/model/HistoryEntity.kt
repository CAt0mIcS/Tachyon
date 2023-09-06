package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId

const val HISTORY_DATABASE_TABLE_NAME = "History"

@Entity(tableName = HISTORY_DATABASE_TABLE_NAME)
data class HistoryEntity(
    @PrimaryKey
    var mediaId: MediaId,
    var timestamp: Long = System.currentTimeMillis()
)