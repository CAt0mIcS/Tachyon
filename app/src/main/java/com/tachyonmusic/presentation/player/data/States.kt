package com.tachyonmusic.presentation.player.data

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.SinglePlayback

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var artwork: Artwork? = null,
    var children: List<SinglePlayback> = emptyList()
)
