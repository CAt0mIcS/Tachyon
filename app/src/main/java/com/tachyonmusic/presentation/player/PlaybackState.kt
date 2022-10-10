package com.tachyonmusic.presentation.player

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var durationString: String = "",
)