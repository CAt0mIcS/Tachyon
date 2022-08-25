package com.tachyonmusic.media.playback

import android.graphics.Bitmap
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.tachyonmusic.media.data.MediaId
import com.google.android.exoplayer2.MediaItem
import kotlinx.serialization.Transient
import java.io.File


//@Serializable
sealed class Playback : Parcelable {
    abstract val mediaId: MediaId
    abstract val path: File?

    abstract val title: String?
    abstract val artist: String?
    abstract val duration: Long?
    abstract val albumArt: Bitmap?

    enum class Type(val value: Int) {
        SongSharedStorage(0), Loop(1), Playlist(2);

        companion object {
            fun fromLong(value: Long) = values().first { it.value.toLong() == value }
        }

        override fun toString() = "*$value*"
    }

    companion object {
//        private val module = SerializersModule {
//            polymorphic(Playback::class) {
//                polymorphic(SinglePlayback::class) {
//                    subclass(Song::class, serializer<Song>())
//                    subclass(Loop::class, serializer<Loop>())
//                }
//                subclass(Playlist::class, serializer<Playlist>())
//            }
//        }

//        val JSON = Json {
//            // TODO: This isn't nice
//            useArrayPolymorphism = true
//            serializersModule = module
//        }

        fun createFromHashMap(map: HashMap<String, Any?>) =
            when (Type.fromLong(map["type"]!! as Long)) {
                Type.SongSharedStorage -> Song.createFromHashMap(map)
                Type.Loop -> Loop.createFromHashMap(map)
                Type.Playlist -> Playlist.createFromHashMap(map)
            }
    }

    @Transient
    var onStartTimeChanged: (((Long /*startTime*/) -> Unit))? = null

    @Transient
    var onEndTimeChanged: (((Long /*endTime*/) -> Unit))? = null

    abstract var startTime: Long
    abstract var endTime: Long

    abstract fun toMediaMetadata(): MediaMetadataCompat
    abstract fun toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem
    abstract fun toMediaDescriptionCompat(): MediaDescriptionCompat

    abstract fun toHashMap(): HashMap<String, Any?>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}

//@Serializable
sealed class SinglePlayback : Playback() {
    abstract override val duration: Long

    abstract fun toExoPlayerMediaItem(): MediaItem
}

