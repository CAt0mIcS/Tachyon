package com.tachyonmusic.core.data.constants

import com.tachyonmusic.core.data.playback.LocalRemix
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.playback.Playback

sealed class PlaybackType(val value: Int) {
    sealed class Song(value: Int) : PlaybackType(value) {
        class Local : Song(0)
    }

    sealed class Remix(value: Int) : PlaybackType(value) {
        class Local : Remix(1)
    }

    sealed class Playlist(value: Int) : PlaybackType(value) {
        class Local : Playlist(2)
    }

    sealed class Ad(value: Int) : PlaybackType(value) {
        class Banner : Ad(3)
    }

    companion object {
        fun build(value: String): PlaybackType {
            return when (value) {
                "*0*" -> Song.Local()
                "*1*" -> Remix.Local()
                "*2*" -> Playlist.Local()
                "*3*" -> Ad.Banner()
                else -> TODO("Unsupported value $value for playback type")
            }
        }

        fun build(playback: Playback?): PlaybackType {
            return when (playback) {
                is LocalSong? -> Song.Local()
                is LocalRemix? -> Remix.Local()
                is LocalPlaylist? -> Playlist.Local()
                else -> TODO("Unknown playback type ${playback?.javaClass?.name}")
            }
        }
    }

    override fun toString() = "*$value*"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaybackType) return false

        if (value != other.value) return false

        return true
    }
}

