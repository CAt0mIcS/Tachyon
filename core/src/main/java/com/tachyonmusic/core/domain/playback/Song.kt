package com.tachyonmusic.core.domain.playback

import android.os.Bundle
import android.os.Parcel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData

abstract class Song(
    mediaId: MediaId,
    final override val title: String,
    final override val artist: String,
    final override val duration: Long
) : SinglePlayback(mediaId) {

    final override val timingData: ArrayList<TimingData> = arrayListOf(TimingData(0L, duration))

    abstract override val playbackType: PlaybackType.Song

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.source
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
            putStringArray(MetadataKeys.TimingData, emptyArray())
            putParcelable(MetadataKeys.Playback, this@Song)
        })
    }.build()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mediaId.source)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration)
    }

    override fun describeContents() = 0

}