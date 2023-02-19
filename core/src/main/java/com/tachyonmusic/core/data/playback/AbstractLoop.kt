package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.MutableStateFlow

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
    override val duration: Duration
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    abstract override val playbackType: PlaybackType.Loop

    override val artwork: MutableStateFlow<Artwork?>
        get() = song.artwork
    override val isArtworkLoading = song.isArtworkLoading


    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "timingData" to timingData.timingData
    )

    override fun toMediaItem() = toMediaItem(null)

    override fun toMediaItem(associatedPlaylist: Playlist?) = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setUri(uri)
        setMediaMetadata(toMediaMetadata(associatedPlaylist))
    }.build()

    private fun toMediaMetadata(associatedPlaylist: Playlist?) = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration.inWholeMilliseconds)
            putParcelable(MetadataKeys.TimingData, timingData)
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@AbstractLoop)

            if (associatedPlaylist != null)
                putParcelable(MetadataKeys.AssociatedPlaylist, associatedPlaylist)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(timingData, flags)
        parcel.writeParcelable(song, flags)
    }
}