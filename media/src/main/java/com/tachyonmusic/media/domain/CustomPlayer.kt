package com.tachyonmusic.media.domain

import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage


interface CustomPlayer : Player {
    fun cancelLoopMessage(): PlayerMessage?
    fun postLoopMessage(startTime: Long, endTime: Long)
    fun postLoopMessageForPlaylist(endTime: Long)
}