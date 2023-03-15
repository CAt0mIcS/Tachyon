package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration

abstract class AbstractLoop(
    final override val mediaId: MediaId,
    final override val name: String,
    final override var timingData: TimingDataController?,
    final override val song: Song
) : Loop, AbstractPlayback() {

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Duration
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    override var artworkType: String
        get() = song.artworkType
        set(value) {
            song.artworkType = value
        }
    abstract override val playbackType: PlaybackType.Loop

    override val artwork = song.artwork
    override val isArtworkLoading = song.isArtworkLoading
    override val isPlayable = song.isPlayable


    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "timingData" to timingData?.timingData
    )

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata())
    }.build()

    private fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(isPlayable.value)

        // EmbeddedArtwork automatically handled by media3
        when (val artworkVal = artwork.value) {
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
}