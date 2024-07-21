package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.playback_layers.SortingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val songFlow: Flow<List<Song>>
    val remixFlow: Flow<List<Remix>>
    val playlistFlow: Flow<List<Playlist>>

    val historyFlow: Flow<List<SinglePlayback>>

    suspend fun getSongs(): List<Song>
    suspend fun getRemixes(): List<Remix>
    suspend fun getPlaylists(): List<Playlist>

    suspend fun getHistory(): List<SinglePlayback>

    val sortingPreferences: StateFlow<SortingPreferences>
    fun setSortingPreferences(sortPrefs: SortingPreferences)
}