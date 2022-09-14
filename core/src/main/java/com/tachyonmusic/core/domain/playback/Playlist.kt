package com.tachyonmusic.core.domain.playback

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController

abstract class Playlist(
    mediaId: MediaId,
    val name: String,
    playbacks: MutableList<SinglePlayback>,
    currentPlaylistIndex: Int = 0
) : Playback(mediaId) {

    val playbacks: List<SinglePlayback>
        get() = _playbacks

    var currentPlaylistIndex: Int

    private val _playbacks: MutableList<SinglePlayback>

    override val title: String?
        get() = current?.title
    override val artist: String?
        get() = current?.artist
    override val duration: Long?
        get() = current?.duration

    override val uri: Uri?
        get() = current?.uri

    override val timingData: TimingDataController?
        get() = current?.timingData

    abstract override val playbackType: PlaybackType.Playlist


    val current: SinglePlayback?
        get() = if (currentPlaylistIndex != -1 && currentPlaylistIndex < playbacks.size) playbacks[currentPlaylistIndex] else null

    init {
        _playbacks = playbacks
        this.currentPlaylistIndex = currentPlaylistIndex
    }


    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
        setIsPlayable(true)
        setExtras(Bundle().apply {
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@Playlist)
        })
    }.build()

    fun toMediaItemList(): List<MediaItem> = playbacks.map { it.toMediaItem() }

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.source,
        "currPlIdx" to currentPlaylistIndex,
        "playbacks" to playbacks.map { it.toHashMap() }
    )

    operator fun get(i: Int): SinglePlayback = playbacks[i]

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelableArray(playbacks.toTypedArray(), flags)
        parcel.writeInt(currentPlaylistIndex)
    }
}