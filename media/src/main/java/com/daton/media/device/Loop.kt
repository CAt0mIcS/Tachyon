package com.daton.media.device

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MetadataKeys
import com.daton.media.ext.*
import com.google.android.exoplayer2.MediaItem
import java.io.File

data class Loop(
    val name: String,
    override var startTime: Long,
    override var endTime: Long,
    val song: Song
) : SinglePlayback() {

    override val path: File
        get() = song.path
    override val title: String
        get() = song.title
    override val artist: String
        get() = song.artist
    override val duration: Long
        get() = song.duration
    override val albumArt: Bitmap?
        get() = song.albumArt

    override val mediaId: String
        get() = "*loop*$path"

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readParcelable(Song::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeParcelable(song, flags)
    }

    override fun toMediaMetadata(): MediaMetadataCompat =
        MediaMetadataCompat.Builder().also { metadata ->
            metadata.mediaId = mediaId
            metadata.loopName = name
            metadata.startTime = startTime
            metadata.endTime = endTime

            metadata.title = song.title
            metadata.artist = song.artist
            metadata.albumArt = song.albumArt
            metadata.duration = song.duration
        }.build()

    override fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(toMediaDescriptionCompat(), 0)

    override fun toMediaDescriptionCompat(): MediaDescriptionCompat =
        MediaDescriptionCompat.Builder().also { desc ->
            desc.setMediaId(mediaId)
            desc.setExtras(Bundle().apply { putParcelable(MetadataKeys.Playback, this@Loop) })
        }.build()

    override fun toExoPlayerMediaItem(): MediaItem =
        MediaItem.Builder().apply {
            setMediaId(mediaId)
            setUri(Uri.parse(path.absolutePath))

            setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().apply {
                setTitle(title)
                setArtist(artist)

                val bundle = Bundle()
                bundle.putLong(MetadataKeys.Duration, duration)
                bundle.putLong(MetadataKeys.StartTime, startTime)
                bundle.putLong(MetadataKeys.EndTime, endTime)
                bundle.putParcelable(MetadataKeys.Playback, this@Loop)
                setExtras(bundle)
            }.build())
        }.build()


    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Loop> {
        override fun createFromParcel(parcel: Parcel): Loop = Loop(parcel)
        override fun newArray(size: Int): Array<Loop?> = arrayOfNulls(size)
    }
}