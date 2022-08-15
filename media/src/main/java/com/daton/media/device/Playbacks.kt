package com.daton.media.device

import android.graphics.Bitmap
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.google.android.exoplayer2.MediaItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File


@Serializable
abstract class Playback : Parcelable {
    abstract val mediaId: MediaId
    abstract val path: File?

    @Transient
    var onStartTimeChanged: (((Long /*startTime*/) -> Unit))? = null

    @Transient
    var onEndTimeChanged: (((Long /*endTime*/) -> Unit))? = null

    abstract var startTime: Long
    abstract var endTime: Long

    abstract fun toMediaMetadata(): MediaMetadataCompat
    abstract fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem
    abstract fun toMediaDescriptionCompat(): MediaDescriptionCompat
    abstract fun toExoPlayerMediaItem(): MediaItem

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}

abstract class SinglePlayback : Playback() {
    abstract val title: String?
    abstract val artist: String?
    abstract val duration: Long
    abstract val albumArt: Bitmap?
}

