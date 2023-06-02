package com.tachyonmusic.core.data.constants

import com.tachyonmusic.core.data.playback.LocalCustomizedSongImpl
import com.tachyonmusic.core.data.playback.LocalPlaylistImpl
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Playback

sealed class PlaybackType(val value: Int) {
    sealed class Song(value: Int) : PlaybackType(value) {
        class Local : Song(0)
    }

    sealed class CustomizedSong(value: Int) : PlaybackType(value) {
        class Local : CustomizedSong(1)
    }

    sealed class Playlist(value: Int) : PlaybackType(value) {
        class Local : Playlist(2)
    }

    companion object {
        fun build(value: String): PlaybackType {
            return when (value) {
                "*0*" -> Song.Local()
                "*1*" -> CustomizedSong.Local()
                "*2*" -> Playlist.Local()
                else -> TODO("Unsupported value $value for playback type")
            }
        }

        fun build(playback: Playback?): PlaybackType {
            return when(playback) {
                is LocalSongImpl? -> Song.Local()
                is LocalCustomizedSongImpl? -> CustomizedSong.Local()
                is LocalPlaylistImpl? -> Playlist.Local()
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

