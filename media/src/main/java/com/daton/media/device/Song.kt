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
import com.daton.media.data.SongMetadata
import com.daton.media.ext.*
import com.google.android.exoplayer2.MediaItem
import java.io.File

class Song : SinglePlayback {

    override val path: File
    override val title: String
    override val artist: String
    override val duration: Long
    override val albumArt: Bitmap?

    override val mediaId: String
        get() = "*song*${path.path}"

    constructor(path: File) {
        this.path = path
        SongMetadata(path).let { metadata ->
            title = metadata.title
            artist = metadata.artist
            duration = metadata.duration
            albumArt = metadata.albumArt
        }
    }

    constructor(
        path: File,
        title: String,
        artist: String,
        duration: Long,
        albumArt: Bitmap? = null
    ) {
        this.path = path
        this.title = title
        this.artist = artist
        this.duration = duration
        this.albumArt = albumArt
    }


    constructor(parcel: Parcel) : this(
        File(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readParcelable(Bitmap::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path.absolutePath)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration)
        parcel.writeParcelable(albumArt, flags)
    }


    override fun toMediaMetadata(): MediaMetadataCompat =
        MediaMetadataCompat.Builder().also { metadata ->
            metadata.mediaId = mediaId
            metadata.path = path
            metadata.title = title
            metadata.artist = artist
            metadata.duration = duration
            metadata.albumArt = albumArt
        }.build()

    override fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(toMediaDescriptionCompat(), 0)

    override fun toMediaDescriptionCompat(): MediaDescriptionCompat =
        MediaDescriptionCompat.Builder().also { desc ->
            desc.setMediaId(mediaId)
            desc.setExtras(Bundle().apply { putParcelable(MetadataKeys.Playback, this@Song) })
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
                bundle.putParcelable(MetadataKeys.Playback, this@Song)
                setExtras(bundle)
            }.build())
        }.build()


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song = Song(parcel)
        override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
    }
}