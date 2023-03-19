package com.tachyonmusic.core.domain.playback

interface Playlist : Playback {
    val name: String
    val playbacks: List<SinglePlayback>
    var currentPlaylistIndex: Int
    val current: SinglePlayback?

    fun add(playback: SinglePlayback)
    fun remove(playback: SinglePlayback)

    fun hasPlayback(playback: SinglePlayback) = playbacks.contains(playback)

    override fun copy(): Playlist
}
