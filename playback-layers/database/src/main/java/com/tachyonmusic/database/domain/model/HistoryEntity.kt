package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.util.MediaIdSerializer
import kotlinx.serialization.Serializable

const val HISTORY_DATABASE_TABLE_NAME = "History"

@Serializable
@Entity(tableName = HISTORY_DATABASE_TABLE_NAME)
data class HistoryEntity(
    @Serializable(with = MediaIdSerializer::class)
    @PrimaryKey
    var mediaId: MediaId,
    var timestamp: Long = System.currentTimeMillis()
)