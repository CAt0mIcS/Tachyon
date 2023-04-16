package com.tachyonmusic.permission.domain

import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.Flow

interface PermissionMapperRepository {

    val songFlow: Flow<List<Song>>
    val customizedSongFlow: Flow<List<CustomizedSong>>
    val playlistFlow: Flow<List<Playlist>>

    val historyFlow: Flow<List<SinglePlayback>>


    suspend fun getSongs(): List<Song>
    suspend fun getCustomizedSongs(): List<CustomizedSong>
    suspend fun getPlaylists(): List<Playlist>

    suspend fun getHistory(): List<SinglePlayback>
}