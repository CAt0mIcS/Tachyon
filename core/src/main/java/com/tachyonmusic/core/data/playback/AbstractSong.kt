package com.tachyonmusic.core.data.playback

import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.MutableStateFlow

abstract class AbstractSong(
    final override val mediaId: MediaId,
    final override val title: String,
    final override val artist: String,
    final override val duration: Long,
) : Song, AbstractPlayback() {

    final override var timingData = TimingDataController(emptyList())

    abstract override val playbackType: PlaybackType.Song

    override val artwork = MutableStateFlow<Artwork?>(null)

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString()
    )

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration)

            // Empty here to allow custom setting of timing data
            putParcelable(MetadataKeys.TimingData, TimingDataController())
            putParcelable(MetadataKeys.Playback, this@AbstractSong)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration)
    }
}