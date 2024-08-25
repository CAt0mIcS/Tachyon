package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import java.net.URI

data class Playback(
    val mediaId: MediaId,
    val title: String,
    val artist: String,
    val duration: Duration,
    val isPlayable: Boolean,

    val album: String? = null,
    val artwork: Artwork? = null,
    val isArtworkLoading: Boolean = false,
    val timingData: TimingDataController = TimingDataController.default(duration),
    val isHidden: Boolean = false,

    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val equalizerBands: List<EqualizerBand>? = null,
    val playbackParameters: PlaybackParameters = PlaybackParameters(),
    val reverb: ReverbConfig? = null,

    val timestampCreatedAddedEdited: Long = System.currentTimeMillis()
) {

    val timingDataLoopingEnabled: Boolean
        get() = !timingData.isNullOrEmpty()
    val bassBoostEnabled: Boolean
        get() = bassBoost != 0
    val virtualizerEnabled: Boolean
        get() = virtualizerStrength != 0
    val equalizerEnabled: Boolean
        get() = !equalizerBands.isNullOrEmpty()
    val reverbEnabled: Boolean
        get() = reverb != null

    val playbackType: PlaybackType
        get() = mediaId.playbackType
    val isRemix: Boolean
        get() = playbackType is PlaybackType.Remix
    val isSong: Boolean
        get() = playbackType is PlaybackType.Song

    val hasArtwork: Boolean
        get() = artwork != null || isArtworkLoading

    /**
     * Song-specific properties
     */
    val uri: Uri?
        get() = if (isSong) mediaId.uri!! else null

    /**
     * Remix-specific properties
     */
    val songMediaId: MediaId?
        get() = if (isRemix) mediaId.underlyingMediaId!! else null
    val name: String?
        get() = if (isRemix) mediaId.name!! else null


    fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    private fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setIsBrowsable(false)
        setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        setIsPlayable(isPlayable)

        // EmbeddedArtwork automatically handled by media3
        when (val artworkVal = artwork) {
            null -> {}
            is RemoteArtwork -> setArtworkUri(Uri.parse(artworkVal.uri.toString()))
        }

        setTitle(title)
        setDisplayTitle(if (isRemix) "$name - $title" else title)
        setArtist(artist)
        setAlbumTitle(album)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration.inWholeMilliseconds)
            putParcelable(MetadataKeys.TimingData, timingData)
            putLong(MetadataKeys.TimestampCreatedAddedEdited, timestampCreatedAddedEdited)
            putBoolean(MetadataKeys.IsHidden, isHidden)

            putInt(MetadataKeys.BassBoost, bassBoost)
            putInt(MetadataKeys.VirtualizerStrength, virtualizerStrength)
            putParcelableArrayList(
                MetadataKeys.EqualizerBands,
                ArrayList(equalizerBands ?: emptyList())
            )
            putParcelable(MetadataKeys.PlaybackParameters, playbackParameters)
            putParcelable(MetadataKeys.Reverb, reverb)

            if (artwork is EmbeddedArtwork) {
                putString(MetadataKeys.EmbeddedArtworkUri, artwork.uri.toString())
            }
        })
    }.build()

    override fun toString() = mediaId.toString()

    fun toBundle() = toMediaItem().toBundle()

    companion object {
        fun fromMediaItem(mediaItem: MediaItem): Playback {
            val extras = mediaItem.mediaMetadata.extras!!

            val remoteArtworkUri = mediaItem.mediaMetadata.artworkUri
            val embeddedArtworkUri = extras.getString(MetadataKeys.EmbeddedArtworkUri)?.let {
                Uri.parse(it)
            }

            return Playback(
                MediaId.deserialize(mediaItem.mediaId),
                mediaItem.mediaMetadata.title!!.toString(),
                mediaItem.mediaMetadata.artist!!.toString(),
                extras.getLong(MetadataKeys.Duration).ms,
                mediaItem.mediaMetadata.isPlayable!!,
                mediaItem.mediaMetadata.albumTitle?.toString(),
                if (embeddedArtworkUri != null)
                    EmbeddedArtwork(null, embeddedArtworkUri)
                else if (remoteArtworkUri != null)
                    RemoteArtwork(URI(remoteArtworkUri.toString()))
                else
                    null,
                isArtworkLoading = false,
                extras.getParcelable(MetadataKeys.TimingData)!!,
                extras.getBoolean(MetadataKeys.IsHidden),
                extras.getInt(MetadataKeys.BassBoost),
                extras.getInt(MetadataKeys.VirtualizerStrength),
                extras.getParcelableArrayList<EqualizerBand>(MetadataKeys.EqualizerBands)!!.toList()
                    .ifEmpty { null },
                extras.getParcelable(MetadataKeys.PlaybackParameters)!!,
                extras.getParcelable(MetadataKeys.Reverb),
                extras.getLong(MetadataKeys.TimestampCreatedAddedEdited)
            )
        }

        fun fromBundle(bundle: Bundle) = fromMediaItem(MediaItem.CREATOR.fromBundle(bundle))
    }
}