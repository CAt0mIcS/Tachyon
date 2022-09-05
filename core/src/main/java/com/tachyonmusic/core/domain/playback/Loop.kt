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

abstract class Loop(
    mediaId: MediaId,
    val name: String,
    final override var timingData: ArrayList<TimingData>,
    val song: Song
) : SinglePlayback(mediaId) {

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Long
        get() = song.duration

    override val uri: Uri
        get() = song.uri

    abstract override val playbackType: PlaybackType.Loop

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "timingData" to timingData
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
            putStringArray(MetadataKeys.TimingData, TimingData.toStringArray(timingData))
            putString(MetadataKeys.Name, name)
            putParcelable(MetadataKeys.Playback, this@Loop)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeStringArray(TimingData.toStringArray(timingData))
        parcel.writeParcelable(song, flags)
    }

    override fun describeContents() = 0
}