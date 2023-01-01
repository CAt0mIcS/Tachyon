package com.tachyonmusic.presentation.player.data

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.SinglePlayback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var artwork: StateFlow<Artwork?> = MutableStateFlow(null),
    var isArtworkLoading: StateFlow<Boolean> = MutableStateFlow(true),
    var children: List<SinglePlayback> = emptyList()
)
