package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration

abstract class AbstractRemix(
    final override val mediaId: MediaId,
    final override val song: Song
) : Remix {


    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Duration
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    abstract override val playbackType: PlaybackType.Remix

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

    override var timingData: TimingDataController? = null
    override var bassBoost: Int? = null
    override var virtualizerStrength: Int? = null
    override var equalizerBands: List<EqualizerBand>? = null
    override var playbackParameters: PlaybackParameters? = null
    override var reverb: ReverbConfig? = null

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

        setTitle("$name - $title")
        setArtist(artist)
        setAlbumTitle(album)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration.inWholeMilliseconds)
            putParcelable(MetadataKeys.TimingData, timingData)
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@AbstractRemix)
            putLong(MetadataKeys.TimestampCreatedAddedEdited, timestampCreatedAddedEdited)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(song, flags)
        parcel.writeLong(timestampCreatedAddedEdited)
        parcel.writeParcelable(timingData, flags)
        parcel.writeInt(bassBoost ?: 0)
        parcel.writeInt(virtualizerStrength ?: 0)
        parcel.writeList(equalizerBands?.map { it.toString() })
        parcel.writeParcelable(playbackParameters, flags)
        parcel.writeParcelable(reverb, flags)
    }

    override fun toString() = mediaId.toString()

    override fun describeContents() = 0

    override fun equals(other: Any?) =
        other is AbstractRemix && mediaId == other.mediaId &&
                song == other.song && timingData == other.timingData && bassBoost == other.bassBoost &&
                virtualizerStrength == other.virtualizerStrength && playbackParameters == other.playbackParameters &&
                equalizerBands == other.equalizerBands && reverb == other.reverb &&
                timestampCreatedAddedEdited == other.timestampCreatedAddedEdited
}