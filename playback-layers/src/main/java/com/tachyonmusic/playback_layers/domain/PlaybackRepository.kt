package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.playback_layers.SortingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val songFlow: SharedFlow<List<Playback>>
    val remixFlow: SharedFlow<List<Playback>>
    val playlistFlow: SharedFlow<List<Playlist>>

    val historyFlow: SharedFlow<List<Playback>>

    val songs: List<Playback>
    val remixes: List<Playback>
    val playlists: List<Playlist>

    val history: List<Playback>

    val sortingPreferences: StateFlow<SortingPreferences>
    fun setSortingPreferences(sortPrefs: SortingPreferences)
}