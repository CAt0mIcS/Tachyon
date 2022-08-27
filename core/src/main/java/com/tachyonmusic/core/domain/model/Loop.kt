package com.tachyonmusic.core.domain.model

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import java.io.File


class Loop(
    val name: String,
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

    override var startTime: Long = 0L
    override var endTime: Long = song.duration

    override val mediaId: MediaId = MediaId(this)

    constructor(name: String, startTime: Long, endTime: Long, song: Song) : this(name, song) {
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(mediaId: MediaId, startTime: Long, endTime: Long) : this(
        mediaId.source.replace(Type.Loop.toString(), ""),
        startTime,
        endTime,
        Song(mediaId.underlyingMediaId!!)
    )

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


    override fun describeContents(): Int = 0

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "mediaId" to mediaId.toString(),
        "startTime" to startTime,
        "endTime" to endTime
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Loop> {
            override fun createFromParcel(parcel: Parcel): Loop = Loop(parcel)
            override fun newArray(size: Int): Array<Loop?> = arrayOfNulls(size)
        }

        fun createFromHashMap(map: HashMap<String, Any?>): Loop {
            val mediaId = MediaId.deserialize(map["mediaId"]!! as String)
            return Loop(
                mediaId,
                map["startTime"]!! as Long,
                map["endTime"]!! as Long,
            )
        }
    }
}