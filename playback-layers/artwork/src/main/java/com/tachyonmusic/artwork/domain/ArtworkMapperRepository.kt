package com.tachyonmusic.artwork.domain

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow

interface ArtworkMapperRepository {
    val songFlow: Flow<List<Song>>
    val loopFlow: Flow<List<Loop>>
    val playlistFlow: Flow<List<Playlist>>

    val historyFlow: Flow<List<SinglePlayback>>


    suspend fun getSongs(): List<Song>
    suspend fun getLoops(): List<Loop>
    suspend fun getPlaylists(): List<Playlist>

    suspend fun getHistory(): List<SinglePlayback>
}