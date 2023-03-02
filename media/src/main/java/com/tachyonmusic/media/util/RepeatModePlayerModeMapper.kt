package com.tachyonmusic.media.util

import androidx.media3.common.Player
import com.tachyonmusic.core.RepeatMode


var Player.coreRepeatMode: RepeatMode
    get() = when (repeatMode) {
        Player.REPEAT_MODE_ALL -> if (shuffleModeEnabled) RepeatMode.Shuffle else RepeatMode.All
        Player.REPEAT_MODE_ONE -> RepeatMode.One
        else -> TODO("Invalid repeat mode $repeatMode")
    }
    set(value) {
        when (value) {
            RepeatMode.All -> {
                shuffleModeEnabled = false
                repeatMode = Player.REPEAT_MODE_ALL
            }

            RepeatMode.One -> {
                shuffleModeEnabled = false
                repeatMode = Player.REPEAT_MODE_ONE
            }

            RepeatMode.Shuffle -> {
                repeatMode = Player.REPEAT_MODE_ALL
                shuffleModeEnabled = true
            }
        }
    }