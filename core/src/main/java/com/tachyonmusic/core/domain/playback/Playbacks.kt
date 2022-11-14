package com.tachyonmusic.core.domain.playback

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController

interface Playback : Parcelable {
    val title: String?
    val artist: String?
    val duration: Long?

    val mediaId: MediaId

    val timingData: TimingDataController?

    val uri: Uri?

    val playbackType: PlaybackType

    fun toMediaItem(): MediaItem
    fun toMediaMetadata(): MediaMetadata

    fun toHashMap(): HashMap<String, Any?>

    suspend fun loadBitmap(onDone: suspend () -> Unit = {})

    override fun equals(other: Any?): Boolean

    override fun toString(): String
}

interface SinglePlayback : Playback {
    override val title: String
    override val artist: String
    override val duration: Long

    val artwork: Bitmap?

    fun unloadArtwork()

    override var timingData: TimingDataController

    override val uri: Uri
}