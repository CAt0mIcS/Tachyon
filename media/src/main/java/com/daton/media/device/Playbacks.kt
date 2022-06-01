package com.daton.media.device

import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.SongMetadata
import com.daton.media.ext.*
import kotlinx.serialization.Serializable
import java.io.File


@Serializable
class Loop {

    constructor(mediaId: String, songPath: String, startTime: Long, endTime: Long) {
        this.mediaId = mediaId
        this.songPath = songPath
        this.startTime = startTime
        this.endTime = endTime
    }

//    var timestamp: Long = System.currentTimeMillis()
//        private set

    var mediaId: String = MediaId.Empty
        private set

    var songPath: String = ""
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

    fun toMediaMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().apply {
            mediaId = this@Loop.mediaId
            path = File(this@Loop.songPath)
            startTime = this@Loop.startTime
            endTime = this@Loop.endTime

            SongMetadata(File(this@Loop.songPath)).let { songMetadata ->
                title = songMetadata.title
                artist = songMetadata.artist
            }

        }.build()
    }

    override operator fun equals(other: Any?): Boolean {
        if (other == null || other !is Loop)
            return false

        return mediaId == other.mediaId && songPath == other.songPath && startTime == other.startTime && endTime == other.endTime
    }
}


@Serializable
class Playlist : ArrayList<String>() {
//    var timestamp: Long = System.currentTimeMillis()
//        private set

    var mediaId: String = MediaId.Empty
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