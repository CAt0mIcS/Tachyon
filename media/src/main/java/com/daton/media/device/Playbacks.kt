package com.daton.media.device

import android.graphics.Bitmap
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.MediaItem
import java.io.File


abstract class Playback : Parcelable {
    abstract val mediaId: String
    abstract val path: File?

    abstract fun toMediaMetadata(): MediaMetadataCompat
    abstract fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem
    abstract fun toMediaDescriptionCompat(): MediaDescriptionCompat

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}


abstract class SinglePlayback : Playback() {
    abstract val title: String
    abstract val artist: String
    abstract val duration: Long
    abstract val albumArt: Bitmap?

    open var startTime: Long = 0L
    open var endTime: Long = 0L
        get() = duration

    abstract fun toExoPlayerMediaItem(): MediaItem
}

