package com.tachyonmusic.domain.repository

import com.tachyonmusic.core.domain.playback.SinglePlayback
import kotlinx.coroutines.flow.StateFlow

interface PredefinedPlaylistsRepository {
    val songPlaylist: StateFlow<List<SinglePlayback>>
    val customizedSongPlaylist: StateFlow<List<SinglePlayback>>
}