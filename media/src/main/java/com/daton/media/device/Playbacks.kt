package com.daton.media.device

import android.graphics.Bitmap
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.google.android.exoplayer2.MediaItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import java.io.File


@Serializable
abstract class Playback : Parcelable {
    abstract val mediaId: MediaId
    abstract val path: File?

    companion object {
        private val module = SerializersModule {
            polymorphic(Playback::class) {
                polymorphic(SinglePlayback::class) {
                    subclass(Song::class, serializer<Song>())
                    subclass(Loop::class, serializer<Loop>())
                }
                subclass(Playlist::class, serializer<Playlist>())
            }
        }

        val JSON = Json {
            // TODO: This isn't nice
            useArrayPolymorphism = true
            serializersModule = module
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Playback) return false

        if (mediaId != other.mediaId) return false

        return true
    }
}

@Serializable
abstract class SinglePlayback : Playback() {
    abstract val title: String?
    abstract val artist: String?
    abstract val duration: Long
    abstract val albumArt: Bitmap?

    abstract fun toExoPlayerMediaItem(): MediaItem
}

