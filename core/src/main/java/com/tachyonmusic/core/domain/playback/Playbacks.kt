package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import kotlinx.coroutines.flow.MutableStateFlow
import com.tachyonmusic.util.Duration

interface Playback : Parcelable {
    val title: String?
    val artist: String?
    val duration: Duration?

    val mediaId: MediaId

    val timingData: TimingDataController?

    val uri: Uri?

    val artwork: MutableStateFlow<Artwork?>
    val isArtworkLoading: MutableStateFlow<Boolean>

    val playbackType: PlaybackType

    fun toMediaItem(): MediaItem

    fun toHashMap(): HashMap<String, Any?>

    override fun equals(other: Any?): Boolean

    override fun toString(): String

    val hasArtwork: Boolean
        get() = artwork.value != null || isArtworkLoading.value

    /**
     * @return Either the underlying song in the loop or the first song in the playlist
     */
    val underlyingSong: Song?
        get() = when(this) {
            is Song -> this
            is Loop -> song
            is Playlist -> playbacks.firstOrNull()?.underlyingSong
            else -> TODO("Invalid Playback ${this.javaClass.name}")
        }
}

interface SinglePlayback : Playback {
    override val title: String
    override val artist: String
    override val duration: Duration

    fun toMediaItem(associatedPlaylist: Playlist? = null): MediaItem

    override var timingData: TimingDataController

    override val uri: Uri
}