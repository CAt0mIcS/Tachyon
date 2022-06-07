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
            } else {
                SongMetadata(songMediaId.path!!).let { songMetadata ->
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