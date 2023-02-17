package com.tachyonmusic.presentation.player.data

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.ms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.tachyonmusic.util.Duration

data class PlaybackState(
    var title: String = "",
    var artist: String = "",
    var duration: Duration = 0.ms,
    var children: List<SinglePlayback> = emptyList(),
    var playbackType: PlaybackType? = null
)

data class ArtworkState(
    var artwork: StateFlow<Artwork?> = MutableStateFlow(null),
    var isArtworkLoading: StateFlow<Boolean> = MutableStateFlow(true),
)

data class SeekIncrementsState(
    var forward: Duration = 0.ms,
    var backward: Duration = 0.ms
)
