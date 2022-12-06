package com.daton.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData

@Entity
data class LoopEntity(
    val name: String,
    val songMediaId: MediaId,
    val timingData: List<TimingData>,
    val currentTimingDataIndex: Int = 0,
) : PlaybackEntity()
