package com.tachyonmusic.core.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.File


sealed class Playback : Parcelable {
    abstract val mediaId: MediaId
    abstract val path: File?

    abstract val title: String?
    abstract val artist: String?
    abstract val duration: Long?
    abstract val albumArt: Bitmap?

    abstract val startTime: Long?
    abstract val endTime: Long?


    enum class Type(val value: Int) {
        SongSharedStorage(0), Loop(1), Playlist(2);

        companion object {
            fun fromLong(value: Long) = values().first { it.value.toLong() == value }
        }

        override fun toString() = "*$value*"
    }

    companion object {
        fun createFromHashMap(map: HashMap<String, Any?>) =
            when (Type.fromLong(map["type"]!! as Long)) {
                Type.SongSharedStorage -> Song.createFromHashMap(map)
                Type.Loop -> Loop.createFromHashMap(map)
                Type.Playlist -> Playlist.createFromHashMap(map)
            }
    }

    abstract fun toMediaItem(): MediaItem
    abstract fun toMediaMetadata(): MediaMetadata

    abstract fun toHashMap(): HashMap<String, Any?>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}


sealed class SinglePlayback : Playback() {
    abstract override val duration: Long
    abstract override var startTime: Long
    abstract override var endTime: Long
}

