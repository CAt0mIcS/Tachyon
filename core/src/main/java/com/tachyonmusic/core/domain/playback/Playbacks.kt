package com.tachyonmusic.core.domain.playback

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController

sealed class Playback(val mediaId: MediaId) : Parcelable {
    abstract val title: String?
    abstract val artist: String?
    abstract val duration: Long?

    abstract val timingData: TimingDataController?

    abstract val uri: Uri?

    abstract val playbackType: PlaybackType

    abstract fun toMediaItem(): MediaItem
    abstract fun toMediaMetadata(): MediaMetadata

    abstract fun toHashMap(): HashMap<String, Any?>

    abstract suspend fun loadBitmap(onDone: suspend () -> Unit = {})

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }

    override fun toString() = mediaId.toString()
}

sealed class SinglePlayback(mediaId: MediaId) : Playback(mediaId) {
    abstract override val title: String
    abstract override val artist: String
    abstract override val duration: Long

    var artwork: Bitmap? = null
        protected set

    abstract override val timingData: TimingDataController

    abstract override val uri: Uri
}