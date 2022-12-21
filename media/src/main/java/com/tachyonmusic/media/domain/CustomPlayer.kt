package com.tachyonmusic.media.domain

import androidx.media3.common.Player
import com.tachyonmusic.core.domain.TimingDataController


interface CustomPlayer : Player {
    fun updateTimingData(newTimingData: TimingDataController)
}