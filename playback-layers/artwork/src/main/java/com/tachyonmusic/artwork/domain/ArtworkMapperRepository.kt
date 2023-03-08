package com.tachyonmusic.artwork.domain

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow

interface ArtworkMapperRepository {
    val songs: Flow<List<Song>>
    val loops: Flow<List<Loop>>
    val playlists: Flow<List<Playlist>>

    val history: Flow<List<SinglePlayback>>
}