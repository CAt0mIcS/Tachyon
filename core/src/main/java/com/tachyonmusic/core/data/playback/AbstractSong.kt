package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration

abstract class AbstractSong(
    final override val mediaId: MediaId,
    final override val title: String,
    final override val artist: String,
    final override val duration: Duration
) : Song {

    final override var timingData: TimingDataController? = null

    abstract override val playbackType: PlaybackType.Song

    override var isPlayable: Boolean = false

    override var artwork: Artwork? = null
    override var isArtworkLoading = false


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

            // Empty here to allow custom setting of timing data
            putParcelable(MetadataKeys.TimingData, TimingDataController())
            putParcelable(MetadataKeys.Playback, this@AbstractSong)
        })
    }.build()

    override fun toString() = mediaId.toString()

    override fun describeContents() = 0

    override fun equals(other: Any?) =
        other is AbstractSong && mediaId == other.mediaId && title == other.title &&
                artist == other.artist && duration == other.duration &&
                timingData == other.timingData && isPlayable == other.isPlayable &&
                artwork == other.artwork && isArtworkLoading == other.isArtworkLoading &&
                isHidden == other.isHidden
}