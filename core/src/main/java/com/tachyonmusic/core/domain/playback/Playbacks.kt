package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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

    override fun equals(other: Any?): Boolean

    override fun toString(): String
}

interface SinglePlayback : Playback {
    override val title: String
    override val artist: String
    override val duration: Long

    val artwork: StateFlow<Artwork?>

    fun unloadArtwork()
    suspend fun loadArtwork(imageSize: Int): Flow<Resource<Unit>>

    override var timingData: TimingDataController

    override val uri: Uri
}