package com.tachyonmusic.core.domain.playback

import androidx.media3.common.MediaItem

interface Playlist : Playback {
    fun toMediaItemList(): List<MediaItem>
    fun add(playback: SinglePlayback)
    fun remove(playback: SinglePlayback)

    fun hasPlayback(playback: SinglePlayback) = playbacks.contains(playback)

    val name: String
    val playbacks: List<SinglePlayback>
    val currentPlaylistIndex: Int
    val current: SinglePlayback?
}
