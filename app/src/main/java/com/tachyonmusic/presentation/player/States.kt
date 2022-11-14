package com.tachyonmusic.presentation.player

import android.graphics.Bitmap
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var durationString: String = "",
    var artwork: Bitmap? = null
)
