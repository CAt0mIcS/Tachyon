package com.tachyonmusic.media.domain

import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController


interface CustomPlayer : Player {
    fun updateTimingData(newTimingData: TimingDataController)
}