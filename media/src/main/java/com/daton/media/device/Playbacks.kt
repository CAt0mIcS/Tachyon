package com.daton.media.device

import com.daton.media.ext.MediaId
import kotlinx.serialization.Serializable


@Serializable
class Loop {

    constructor(mediaId: String, songPath: String, startTime: Long, endTime: Long) {
        this.mediaId = mediaId
        this.songPath = songPath
        this.startTime = startTime
        this.endTime = endTime
    }

    var timestamp: Long = System.currentTimeMillis()
        private set

    var mediaId: String = MediaId.Empty
        private set

    var songPath: String = ""
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    var startTime: Long = 0L
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    var endTime: Long = 0L
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }
}


@Serializable
class Playlist : ArrayList<String>() {
    var timestamp: Long = System.currentTimeMillis()
        private set

    var mediaId: String = MediaId.Empty
        private set

    var currentPlaybackIndex: Int = 0
        set(value) {
            field = value
            timestamp = System.currentTimeMillis()
        }

    operator fun plus(mediaId: String) {
        add(mediaId)
        timestamp = System.currentTimeMillis()
    }

    operator fun minus(mediaId: String) {
        remove(mediaId)
        timestamp = System.currentTimeMillis()
    }
}