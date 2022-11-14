package com.tachyonmusic.core.domain.playback

import androidx.media3.common.MediaItem

interface Playlist : Playback {
    fun toMediaItemList(): List<MediaItem>

    val playbacks: List<SinglePlayback>
    val currentPlaylistIndex: Int
    val current: SinglePlayback?
}
