package com.daton.media.device

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.data.SongMetadata
import com.daton.media.ext.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
abstract class Playback {
    var mediaId: MediaId = MediaId.Empty
        protected set

    abstract fun toMediaMetadata(
        mediaSource: MediaSource? = null,
        baseMediaId: MediaId? = null
    ): MediaMetadataCompat

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}

class Song constructor() : Playback() {

    var title: String = ""
        private set

    var artist: String = ""
        private set

    var albumArt: Bitmap? = null
        private set

    var duration: Long = 0
        private set


    constructor(path: File) : this() {
        mediaId = MediaId.fromSongFile(path)

        SongMetadata(path).let { songMetadata ->
            title = songMetadata.title
            artist = songMetadata.artist
            albumArt = songMetadata.albumArt
            duration = songMetadata.duration
        }
    }

    constructor(
        mediaId: MediaId,
        title: String,
        artist: String,
        duration: Long,
        albumArt: Bitmap? = null
    ) : this() {
        this.mediaId = mediaId
        this.title = title
        this.artist = artist
        this.duration = duration
        this.albumArt = albumArt
    }

    override fun toMediaMetadata(
        mediaSource: MediaSource?,
        baseMediaId: MediaId?
    ): MediaMetadataCompat =
        MediaMetadataCompat.Builder().also {
            if (baseMediaId != null) {
                it.mediaId = baseMediaId + mediaId
            } else
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

    constructor(mediaId: MediaId, startTime: Long, endTime: Long) : this() {
        this.mediaId = mediaId
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

    // TODO: Is it faster to pass [mediaSource] or using [SongMetadata]
    override fun toMediaMetadata(
        mediaSource: MediaSource?,
        baseMediaId: MediaId?
    ): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().also {
            if (baseMediaId != null) {
                it.mediaId = baseMediaId + mediaId
            } else
                it.mediaId = mediaId
            it.startTime = this@Loop.startTime
            it.endTime = this@Loop.endTime

            val songMetadata = mediaSource?.getSong(songMediaId)
            if (mediaSource != null && songMetadata != null) {
                it.title = songMetadata.title
                it.artist = songMetadata.artist
                it.duration = songMetadata.duration
            } else {
                SongMetadata(songMediaId.path!!).let { songMetadata ->
                    it.title = songMetadata.title
                    it.artist = songMetadata.artist
                    it.duration = songMetadata.duration
                }
            }
        }.build()
    }

    override fun toString() = Json.encodeToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Loop

        if (!super.equals(other)) return false
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
class Playlist constructor() : Playback() {
//    var timestamp: Long = System.currentTimeMillis()
//        private set

    constructor(name: String) : this() {
        mediaId = MediaId.fromPlaylist(name)
    }

    constructor(
        mediaId: MediaId,
        playbacks: List<MediaId>,
        currentPlaybackIndex: Int = 0
    ) : this() {
        this.mediaId = mediaId
        this.playbacks.addAll(playbacks)
        this.currentPlaybackIndex = currentPlaybackIndex
    }

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

    override fun toMediaMetadata(
        mediaSource: MediaSource?,
        baseMediaId: MediaId?
    ): MediaMetadataCompat {
        return MediaMetadataCompat.Builder().apply {
            mediaId = this@Playlist.mediaId
            playlistPlaybacks = playbacks
            currentPlaylistPlaybackIndex = currentPlaybackIndex
        }.build()
    }

    fun toMediaMetadataList(mediaSource: MediaSource): List<MediaMetadataCompat> {
        return List(playbacks.size) { i ->
            if (playbacks[i].isSong) {
                (mediaSource.getSong(playbacks[i])
                    ?: TODO("Invalid song ${playbacks[i]}")).toMediaMetadata(mediaSource, mediaId)
            } else if (playbacks[i].isLoop) {
                (mediaSource.getLoop(playbacks[i])
                    ?: TODO("Invalid loop ${playbacks[i]}")).toMediaMetadata(mediaSource, mediaId)
            } else
                TODO("Playback is neither song nor loop, nested playlists are currently not supported")
        }
    }

    override fun toString() = Json.encodeToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playlist) return false
        if (!super.equals(other)) return false

        if (!super.equals(other)) return false
        if (playbacks != other.playbacks) return false
        if (currentPlaybackIndex != other.currentPlaybackIndex) return false

        return true
    }
}