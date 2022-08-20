package com.tachyonmusic.media.playback

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.tachyonmusic.media.data.MediaId
import com.tachyonmusic.media.data.MetadataKeys
import com.tachyonmusic.media.data.SongMetadata
import com.tachyonmusic.media.ext.*
import com.google.android.exoplayer2.MediaItem
import java.io.File

//@Serializable(with = Song.Serializer::class)
class Song : SinglePlayback {

    override val path: File

    override var startTime: Long = 0L
        set(value) {
            val oldStart = field
            field = value
            if (field != oldStart)
                onStartTimeChanged?.invoke(field)
        }

    override var endTime: Long
        set(value) {
            val oldEnd = field
            field = value
            if (field != oldEnd)
                onEndTimeChanged?.invoke(field)
        }

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


    override fun toMediaMetadata(): MediaMetadataCompat =
        MediaMetadataCompat.Builder().also { metadata ->
            metadata.mediaId = mediaId.toString()
            metadata.title = title
            metadata.artist = artist
            metadata.duration = duration
            metadata.albumArt = albumArt
        }.build()

    override fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(
            toMediaDescriptionCompat(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )

    override fun toMediaDescriptionCompat(): MediaDescriptionCompat =
        MediaDescriptionCompat.Builder().also { desc ->
            desc.setMediaId(mediaId.toString())
            desc.setExtras(Bundle().apply { putParcelable(MetadataKeys.Playback, this@Song) })
        }.build()

    override fun toExoPlayerMediaItem(): MediaItem =
        MediaItem.Builder().apply {
            setMediaId(mediaId.toString())
            setUri(Uri.parse(path.absolutePath))

            setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().apply {
                setTitle(title)
                setArtist(artist)

                setExtras(duration, startTime, endTime, this@Song)
            }.build())
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
//
//    class Serializer : KSerializer<Song> {
//        override val descriptor: SerialDescriptor =
//            PrimitiveSerialDescriptor("songPath", PrimitiveKind.STRING)
//
//        override fun serialize(encoder: Encoder, value: Song) {
//            encoder.encodeSerializableValue(
//                kotlinx.serialization.serializer(),
//                value.mediaId
//            )
//        }
//
//        override fun deserialize(decoder: Decoder): Song = Song(
//            decoder.decodeSerializableValue(
//                kotlinx.serialization.serializer<MediaId>()
//            )
//        )
//    }
}