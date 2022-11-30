package com.tachyonmusic.presentation.player.data

import android.graphics.Bitmap
import com.tachyonmusic.core.domain.playback.SinglePlayback

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var artwork: Bitmap? = null,
    var children: List<SinglePlayback> = emptyList()
)
