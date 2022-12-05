package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.*

abstract class AbstractLoop(
    final override val mediaId: MediaId,
    final override val name: String,
    final override var timingData: TimingDataController,
    final override val song: Song
) : Loop, AbstractPlayback() {

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Long
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    abstract override val playbackType: PlaybackType.Loop

    override var artwork: Artwork? = null
        protected set

    override fun unloadArtwork() {
        artwork = null
    }

    override suspend fun loadArtwork(imageSize: Int) = song.loadArtwork(imageSize)

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "timingData" to timingData.timingData
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
}