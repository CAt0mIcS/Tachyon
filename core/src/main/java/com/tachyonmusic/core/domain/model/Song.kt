package com.tachyonmusic.core.domain.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import java.io.File


class Song : SinglePlayback {

    override val path: File

    override var startTime: Long = 0L
    override var endTime: Long

    override val title: String
    override val artist: String
    override val duration: Long
    override val albumArt: Bitmap?

    override val mediaId: MediaId

    constructor(path: File) {
        this.path = path
        SongMetadata(path).let { metadata ->
            title = metadata.title
            artist = metadata.artist
            duration = metadata.duration
            albumArt = metadata.albumArt
        }
        mediaId = MediaId(this)
        endTime = duration
    }

    constructor(mediaId: MediaId) : this(mediaId.path)

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
        mediaId = MediaId(this)
        endTime = duration
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


    override fun toMediaItem(): MediaItem = MediaItem.Builder().apply {
        setMediaId(mediaId.toString())
        setMediaMetadata(toMediaMetadata())
    }.build()

    override fun toMediaMetadata(): MediaMetadata = MediaMetadata.Builder().apply {
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setTitle(title)
        setArtist(artist)
        setExtras(Bundle().apply {
            putLong(MetadataKeys.Duration, duration)
            putParcelable(MetadataKeys.Playback, this@Song)
        })
    }.build()


    override fun describeContents(): Int {
        return 0
    }

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.source
    )

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<Song> {
            override fun createFromParcel(parcel: Parcel): Song = Song(parcel)
            override fun newArray(size: Int): Array<Song?> = arrayOfNulls(size)
        }

        fun createFromHashMap(map: HashMap<String, Any?>) =
            Song(MediaId(map["mediaId"]!! as String))
    }
}