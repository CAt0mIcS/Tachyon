package com.daton.media.device

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
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

private class SongSerializer : KSerializer<Song> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("songPath", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Song) {
        encoder.encodeSerializableValue(serializer<MediaId>(), value.mediaId)
    }

    override fun deserialize(decoder: Decoder): Song = Song(
        decoder.decodeSerializableValue(
            serializer<MediaId>()
        )
    )
}

@Serializable
class Loop(
    val name: String,

    @Serializable(with = SongSerializer::class)
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
        MediaBrowserCompat.MediaItem(toMediaDescriptionCompat(), 0)

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

    companion object CREATOR : Parcelable.Creator<Loop> {
        override fun createFromParcel(parcel: Parcel): Loop = Loop(parcel)
        override fun newArray(size: Int): Array<Loop?> = arrayOfNulls(size)
    }
}