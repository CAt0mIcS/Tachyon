package com.tachyonmusic.core.data.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.MutableStateFlow

abstract class AbstractPlaylist(
    final override val mediaId: MediaId,
    final override val name: String,
    private val _playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : Playlist, AbstractPlayback() {

    final override val playbacks: List<SinglePlayback>
        get() = _playbacks

    final override var currentPlaylistIndex: Int = 0
        protected set

    override val title: String?
        get() = current?.title
    override val artist: String?
        get() = current?.artist
    override val duration: Duration?
        get() = current?.duration

    /**
     * Updated when the next/previous item is played (TODO)
     */
    override val artwork = MutableStateFlow(current?.artwork?.value)
    override val isArtworkLoading = MutableStateFlow(false)

    override val uri: Uri?
        get() = current?.uri

    override val timingData: TimingDataController?
        get() = current?.timingData

    abstract override val playbackType: PlaybackType.Playlist

    final override val current: SinglePlayback?
        get() = if (currentPlaylistIndex != -1 && currentPlaylistIndex < playbacks.size) playbacks[currentPlaylistIndex] else null

    init {
        this.currentPlaylistIndex = currentPlaylistIndex
    }


    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    private fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
        setIsPlayable(true)
        setExtras(Bundle().apply {
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@AbstractPlaylist)
        })
    }.build()

    override fun toMediaItemList(): List<MediaItem> =
        playbacks.map { it.toMediaItem(associatedPlaylist = this) }

    override fun add(playback: SinglePlayback) {
        _playbacks.add(playback)
    }

    override fun remove(playback: SinglePlayback) {
        _playbacks.remove(playback)
    }

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.source,
        "currPlIdx" to currentPlaylistIndex,
        "playbacks" to playbacks.map { it.toHashMap() }
    )

    operator fun get(i: Int): SinglePlayback = playbacks[i]

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableArray(playbacks.toTypedArray(), flags)
        parcel.writeInt(currentPlaylistIndex)
    }
}