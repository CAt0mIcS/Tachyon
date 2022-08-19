package com.daton.media.playback

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.data.MetadataKeys
import com.daton.media.ext.*
import com.google.android.exoplayer2.MediaItem
import kotlinx.serialization.Serializable
import java.io.File


//@Serializable
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
        set(value) {
            val oldStart = field
            field = value
            if (field != oldStart)
                onStartTimeChanged?.invoke(field)
        }

    override var endTime: Long = song.duration
        set(value) {
            val oldEnd = field
            field = value
            if (field != oldEnd)
                onEndTimeChanged?.invoke(field)
        }

    override val mediaId: MediaId = MediaId(this)

    constructor(name: String, startTime: Long, endTime: Long, song: Song) : this(name, song) {
        this.startTime = startTime
        this.endTime = endTime
    }

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
            metadata.mediaId = mediaId.toString()

            metadata.title = song.title
            metadata.artist = song.artist
            metadata.albumArt = song.albumArt
            metadata.duration = song.duration
        }.build()

    override fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(
            toMediaDescriptionCompat(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )

    override fun toMediaDescriptionCompat(): MediaDescriptionCompat =
        MediaDescriptionCompat.Builder().also { desc ->
            desc.setMediaId(mediaId.toString())
            desc.setExtras(Bundle().apply { putParcelable(MetadataKeys.Playback, this@Loop) })
        }.build()

    override fun toExoPlayerMediaItem(): MediaItem =
        MediaItem.Builder().apply {
            setMediaId(mediaId.toString())
            setUri(Uri.parse(path.absolutePath))

            setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().apply {
                setTitle(title)
                setArtist(artist)

                setExtras(duration, startTime, endTime, this@Loop)
            }.build())
        }.build()


    override fun describeContents(): Int = 0

    override fun toHashMap(): HashMap<String, Any?> = hashMapOf(
        "type" to TYPE_LOOP,
        "name" to name,
        "startTime" to startTime,
        "endTime" to endTime,
        "songMediaId" to song.mediaId.source
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Loop> {
            override fun createFromParcel(parcel: Parcel): Loop = Loop(parcel)
            override fun newArray(size: Int): Array<Loop?> = arrayOfNulls(size)
        }

        fun createFromHashMap(map: HashMap<String, Any?>) = Loop(
            map["name"]!! as String,
            map["startTime"]!! as Long,
            map["endTime"]!! as Long,
            Song(MediaId(map["songMediaId"]!! as String))
        )
    }
}