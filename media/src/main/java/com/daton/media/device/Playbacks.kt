package com.daton.media.device

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.data.SongMetadata
import com.daton.media.ext.*
import kotlinx.serialization.Serializable

@Serializable
open class Playback : Any() {
    var mediaId: MediaId = MediaId.Empty
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}

class Song : Playback() {

    val title: String
    val artist: String
    val albumArt: Bitmap?
    val duration: Long

    init {
        SongMetadata(mediaId.path!!).let { songMetadata ->
            title = songMetadata.title
            artist = songMetadata.artist
            albumArt = songMetadata.albumArt
            duration = songMetadata.duration
        }
    }

    fun toMediaMetadata(): MediaMetadataCompat =
        MediaMetadataCompat.Builder().also {
            it.mediaId = mediaId
            it.title = title
            it.artist = artist
            it.albumArt = albumArt
            it.duration = duration
        }.build()!!
}


@Serializable
class Loop constructor() : Playback() {

    //    var timestamp: Long = System.currentTimeMillis()
//        private set

    constructor(loopName: String, startTime: Long, endTime: Long, songMediaId: MediaId) : this() {
        mediaId = MediaId.fromLoop(loopName, songMediaId)
        this.startTime = startTime
        this.endTime = endTime
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

    val songMediaId: MediaId
        get() = mediaId.underlyingMediaId!!

    fun toMediaMetadata(mediaSource: MediaSource? = null): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().apply {
            mediaId = this@Loop.mediaId
//            path = songMediaId.path
            startTime = this@Loop.startTime
            endTime = this@Loop.endTime

            val songMetadata = mediaSource?.getSong(songMediaId)
            if (mediaSource != null && songMetadata != null) {
                title = songMetadata.title
                artist = songMetadata.artist
                duration = songMetadata.duration
            } else {
                SongMetadata(songMediaId.path!!).let { songMetadata ->
                    title = songMetadata.title
                    artist = songMetadata.artist
                    duration = songMetadata.duration
                }
            }


        }.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Loop

        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (songMediaId != other.songMediaId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }
}


@Serializable
class Playlist {
//    var timestamp: Long = System.currentTimeMillis()
//        private set

    var mediaId: MediaId = MediaId.Empty
        private set

    val playbacks: MutableList<MediaId> = mutableListOf()

    var currentPlaybackIndex: Int = 0
        set(value) {
            field = value
//            timestamp = System.currentTimeMillis()
        }

    operator fun plusAssign(mediaId: MediaId) {
        playbacks += mediaId
//        timestamp = System.currentTimeMillis()
    }

    operator fun minusAssign(mediaId: MediaId) {
        playbacks -= mediaId
//        timestamp = System.currentTimeMillis()
    }

    fun toMediaMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().apply {
            mediaId = this@Playlist.mediaId
            title = ""
            artist = ""
        }.build()
    }

    fun toMediaMetadataList(mediaSource: MediaSource): List<MediaMetadataCompat> {
        return List(playbacks.size) { i ->
            if (playbacks[i].isSong) {
                mediaSource.getSong(playbacks[i]) ?: TODO("Invalid song ${playbacks[i]}")
            } else if (playbacks[i].isLoop) {
                (mediaSource.getLoop(playbacks[i])
                    ?: TODO("Invalid loop ${playbacks[i]}")).toMediaMetadata()
            } else
                TODO("Playback is neither song nor loop, nested playlists are currently not supported")
        }
    }
}