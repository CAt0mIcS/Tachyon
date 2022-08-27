package com.tachyonmusic.core.domain.playback

import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId

abstract class Loop(
    mediaId: MediaId,
    val name: String,
    final override var startTime: Long,
    final override var endTime: Long,
    val song: Song
) : SinglePlayback(mediaId) {

    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Long
        get() = song.duration

    abstract override val playbackType: PlaybackType.Loop

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "startTime" to startTime,
        "endTime" to endTime
    )

    override fun toMediaItem() = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata() = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration)
            putLong(MetadataKeys.StartTime, startTime)
            putLong(MetadataKeys.EndTime, endTime)
            putParcelable(MetadataKeys.Playback, this@Loop)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeParcelable(song, flags)
    }

    override fun describeContents() = 0

}