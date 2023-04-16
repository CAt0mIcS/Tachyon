package com.tachyonmusic.sort.domain

import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.sort.domain.model.SortingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SortedPlaybackRepository {
    val sortingPreferences: StateFlow<SortingPreferences>

    val songFlow: Flow<List<Song>>
    val customizedSongFlow: Flow<List<CustomizedSong>>
    val playlistFlow: Flow<List<Playlist>>

    val historyFlow: Flow<List<SinglePlayback>>

    fun setSortingPreferences(sortPrefs: SortingPreferences)

    suspend fun getSongs(): List<Song>
    suspend fun getCustomizedSongs(): List<CustomizedSong>
    suspend fun getPlaylists(): List<Playlist>

    suspend fun getHistory(): List<SinglePlayback>
}