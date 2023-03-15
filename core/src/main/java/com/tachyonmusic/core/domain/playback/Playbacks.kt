package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.MutableStateFlow

interface Playback : Parcelable {
    val title: String?
    val artist: String?
    val duration: Duration?

    val mediaId: MediaId

    val uri: Uri?

    val artwork: MutableStateFlow<Artwork?>
    val isArtworkLoading: MutableStateFlow<Boolean>

    val playbackType: PlaybackType

    fun toMediaItem(): MediaItem

    fun toHashMap(): HashMap<String, Any?>
    fun copy(): Playback

    override fun equals(other: Any?): Boolean
    override fun toString(): String

    val hasArtwork: Boolean
        get() = artwork.value != null || isArtworkLoading.value

    /**
     * @return Either the underlying song in the loop or the first song in the playlist
     */
    val underlyingSong: Song?
        get() = when (this) {
            is Song -> this
            is Loop -> song
            is Playlist -> playbacks.firstOrNull()?.underlyingSong
            else -> TODO("Invalid Playback ${this.javaClass.name}")
        }

    /**
     * @return Either a song, loop or the first item in the playlist (could be song or loop)
     */
    val underlyingSinglePlayback: SinglePlayback?
        get() = when (this) {
            is SinglePlayback -> this
            is Playlist -> playbacks.firstOrNull()?.underlyingSinglePlayback
            else -> TODO("Invalid Playback ${this.javaClass.name}")
        }
}

interface SinglePlayback : Playback {
    override val title: String
    override val artist: String
    override val duration: Duration

    /**
     * Specifies if the current playback is playable. Could not be playable due to uri permissions
     * being removed from the path where the playback is saved
     */
    val isPlayable: MutableStateFlow<Boolean>

    var timingData: TimingDataController?

    override val underlyingSong: Song
    get() = when(this) {
        is Song -> this
        is Loop -> song
        else -> TODO("Invalid SinglePlayback ${this.javaClass.name}")
    }

    override val uri: Uri

    override fun copy(): SinglePlayback
}