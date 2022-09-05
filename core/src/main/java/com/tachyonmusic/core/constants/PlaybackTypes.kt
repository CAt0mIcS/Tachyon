package com.tachyonmusic.core.constants

sealed class PlaybackType(val value: Int) {
    sealed class Song(value: Int) : PlaybackType(value) {
        class Local : Song(0)
    }

    sealed class Loop(value: Int) : PlaybackType(value) {
        class Remote : Loop(1)
    }

    sealed class Playlist(value: Int) : PlaybackType(value) {
        class Remote : Playlist(2)
    }

    companion object {
        fun build(value: String): PlaybackType {
            when (value) {
                "*0*" -> Song.Local()
                "*1*" -> Loop.Remote()
                "*2*" -> Playlist.Remote()
            }
            TODO("Unsupported value $value for playback type")
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

