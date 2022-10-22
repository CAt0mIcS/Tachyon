package com.tachyonmusic.presentation.player

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var durationString: String = "",
)

data class UpdateState(
    var pos: Long = 0L,
    var posStr: String = ""
)

data class LoopState(
    var startTime: Long = 0L,
    var endTime: Long = 0L
)
