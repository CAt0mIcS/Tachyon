package com.daton.media.device

import android.content.Context
import android.os.Environment
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.data.SongMetadata
import com.daton.media.ext.*
import kotlinx.serialization.Serializable
import java.io.File


@Serializable
class Loop {

    //    var timestamp: Long = System.currentTimeMillis()
//        private set

    var mediaId: MediaId = MediaId.Empty
        private set

    var songMediaId: MediaId = MediaId.Empty
        set(value) {
            field = value
//            timestamp = System.currentTimeMillis()
        }

    var startTime: Long = 0L
        set(value) {
            field = value
//            timestamp = System.currentTimeMillis()
        }

    var endTime: Long = 0L
        set(value) {
            field = value
//            timestamp = System.currentTimeMillis()
        }

    fun toMediaMetadata(mediaSource: MediaSource? = null): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().apply {
            mediaId = this@Loop.mediaId
            path = songMediaId.path
            startTime = this@Loop.startTime
            endTime = this@Loop.endTime

            val songMetadata = mediaSource?.get(songMediaId)
            if (mediaSource != null && songMetadata != null) {
                title = songMetadata.title
                artist = songMetadata.artist
            } else {
                SongMetadata(songMediaId.path).let { songMetadata ->
                    title = songMetadata.title
                    artist = songMetadata.artist
                }
            }


        }.build()
    }

    override operator fun equals(other: Any?): Boolean {
        if (other == null || other !is Loop)
            return false

        return mediaId == other.mediaId && songMediaId == other.songMediaId && startTime == other.startTime && endTime == other.endTime
    }
}


@Serializable
class Playlist : ArrayList<String>() {
//    var timestamp: Long = System.currentTimeMillis()
//        private set

    var mediaId: MediaId = MediaId.Empty
        private set

    var currentPlaybackIndex: Int = 0
        set(value) {
            field = value
//            timestamp = System.currentTimeMillis()
        }

    operator fun plus(mediaId: String) {
        add(mediaId)
//        timestamp = System.currentTimeMillis()
    }

    operator fun minus(mediaId: String) {
        remove(mediaId)
//        timestamp = System.currentTimeMillis()
    }
}