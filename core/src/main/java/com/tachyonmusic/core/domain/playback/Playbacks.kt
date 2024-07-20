package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.util.Duration

interface Playback : Parcelable {
    val title: String?
    val artist: String?
    val duration: Duration?

    val mediaId: MediaId

    val uri: Uri?

    val playbackType: PlaybackType

    fun toMediaItem(): MediaItem

    fun copy(): Playback

    override fun equals(other: Any?): Boolean
    override fun toString(): String

    /**
     * @return Either the underlying song in the remix or the first song in the playlist
     */
    val underlyingSong: Song?
        get() = when (this) {
            is Song -> this
            is Remix -> song
            is Playlist -> playbacks.firstOrNull()?.underlyingSong
            else -> TODO("Invalid Playback ${this.javaClass.name}")
        }

    /**
     * @return Either a song, remix or the first item in the playlist (could be song or remix)
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

    val hasArtwork: Boolean
        get() = artwork != null || isArtworkLoading

    var artwork: Artwork?
    var isArtworkLoading: Boolean

    /**
     * Specifies if the current playback is playable. Could not be playable due to uri permissions
     * being removed from the path where the playback is saved
     */
    var isPlayable: Boolean
    val album: String?
    var timingData: TimingDataController?

    override val underlyingSong: Song
    get() = when(this) {
        is Song -> this
        is Remix -> song
        else -> TODO("Invalid SinglePlayback ${this.javaClass.name}")
    }

    override val uri: Uri

    override fun copy(): SinglePlayback
}