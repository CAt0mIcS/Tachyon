package com.tachyonmusic.media.domain

import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import com.tachyonmusic.core.domain.TimingData


interface CustomPlayer : Player {
    fun updateTimingData(newTimingData: ArrayList<TimingData>)
}