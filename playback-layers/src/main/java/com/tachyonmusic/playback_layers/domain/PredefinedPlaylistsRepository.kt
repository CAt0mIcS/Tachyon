package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.playback.SinglePlayback
import kotlinx.coroutines.flow.StateFlow

interface PredefinedPlaylistsRepository {
    val songPlaylist: StateFlow<List<SinglePlayback>>
    val remixPlaylist: StateFlow<List<SinglePlayback>>
}