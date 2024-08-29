package com.tachyonmusic.core.domain.playback

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId

data class Playlist(
    val mediaId: MediaId,
    val playbacks: List<Playback>,
    val currentPlaylistIndex: Int = 0,
    val timestampCreatedAddedEdited: Long = System.currentTimeMillis()
) {
    val current: Playback
        get() = playbacks[currentPlaylistIndex]

    val name: String
        get() = mediaId.name!!

    val playbackType = PlaybackType.Playlist.Local()

    fun hasPlayback(playback: Playback) = playbacks.contains(playback)
    operator fun get(i: Int): Playback = playbacks[i]

    fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setIsBrowsable(true)
        setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
        setIsPlayable(true)
        setTitle(name)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.TimestampCreatedAddedEdited, timestampCreatedAddedEdited)
            putInt(MetadataKeys.Index, currentPlaylistIndex)
            putParcelableArrayList(
                MetadataKeys.Playback,
                ArrayList(playbacks.map { it.toBundle() })
            )
        })
    }.build()

    fun toBundle() = toMediaItem().toBundle()

    companion object {
        fun fromMediaItem(mediaItem: MediaItem): Playlist {
            val extras = mediaItem.mediaMetadata.extras!!

            return Playlist(
                MediaId.deserialize(mediaItem.mediaId),
                extras.getParcelableArrayList<Bundle>(MetadataKeys.Playback)!!.map { Playback.fromBundle(it) },
                extras.getInt(MetadataKeys.Index),
                extras.getLong(MetadataKeys.TimestampCreatedAddedEdited)
            )
        }

        fun fromBundle(bundle: Bundle) = fromMediaItem(MediaItem.CREATOR.fromBundle(bundle))
    }
}