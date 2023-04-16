package com.tachyonmusic.domain.repository

import com.tachyonmusic.core.domain.playback.SinglePlayback

interface PredefinedPlaylistsRepository {
    val songPlaylist: List<SinglePlayback>
    val customizedSongPlaylist: List<SinglePlayback>
}