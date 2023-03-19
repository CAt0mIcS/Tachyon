package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration

abstract class AbstractLoop(
    final override val mediaId: MediaId,
    final override var timingData: TimingDataController?,
    final override val song: Song
) : Loop {

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Duration
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    abstract override val playbackType: PlaybackType.Loop

    override var artwork: Artwork?
        get() = song.artwork
        set(value) {
            song.artwork = value
        }

    override var isArtworkLoading: Boolean
        get() = song.isArtworkLoading
        set(value) {
            song.isArtworkLoading = value
        }

    override var isPlayable: Boolean
        get() = song.isPlayable
        set(value) {
            song.isPlayable = value
        }

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    private fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(isPlayable)

        // EmbeddedArtwork automatically handled by media3
        when (val artworkVal = artwork) {
            null -> {}
            is RemoteArtwork -> setArtworkUri(Uri.parse(artworkVal.uri.toURL().toString()))
        }

        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration.inWholeMilliseconds)
            putParcelable(MetadataKeys.TimingData, timingData)
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@AbstractLoop)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(timingData, flags)
        parcel.writeParcelable(song, flags)
    }

    override fun toString() = mediaId.toString()

    override fun describeContents() = 0

    override fun equals(other: Any?) =
        other is AbstractLoop && mediaId == other.mediaId && artwork == other.artwork &&
                isArtworkLoading == other.isArtworkLoading && isPlayable == other.isPlayable &&
                song == other.song && timingData == other.timingData
}