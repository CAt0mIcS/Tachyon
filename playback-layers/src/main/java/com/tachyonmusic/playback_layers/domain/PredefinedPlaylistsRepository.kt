package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.playback.Playback
import kotlinx.coroutines.flow.StateFlow

interface PredefinedPlaylistsRepository {
    val songPlaylist: StateFlow<List<Playback>>
    val remixPlaylist: StateFlow<List<Playback>>
}