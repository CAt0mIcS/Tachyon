package com.tachyonmusic.media.util

import androidx.media3.common.Player
import com.tachyonmusic.core.RepeatMode

fun RepeatMode.Companion.fromMedia(@Player.RepeatMode repeatMode: Int, shuffleEnabled: Boolean) =
    when (repeatMode) {
        Player.REPEAT_MODE_ALL -> if (shuffleEnabled) RepeatMode.Shuffle else RepeatMode.All
        Player.REPEAT_MODE_ONE -> RepeatMode.One
        else -> error("Invalid media repeat mode $repeatMode with shuffle enabled: $shuffleEnabled")
    }


var Player.coreRepeatMode: RepeatMode
    get() = RepeatMode.fromMedia(repeatMode, shuffleModeEnabled)
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

            null -> TODO("Null not allowed")
        }
    }