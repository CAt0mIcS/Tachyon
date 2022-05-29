package com.daton.media.ext

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.MetadataKeys
import com.google.android.exoplayer2.MediaItem
import java.io.File

/**
 * Useful extensions for [MediaMetadataCompat].
 */
inline val MediaMetadataCompat.mediaId: String
    get() = getString(MetadataKeys.MediaId)

inline var MediaMetadataCompat.Builder.mediaId: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.MediaId, value)
    }


inline val MediaMetadataCompat.title: String
    get() = getString(MetadataKeys.Title)

inline var MediaMetadataCompat.Builder.title: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.Title, value)
    }


inline val MediaMetadataCompat.artist: String
    get() {
        // Artist not set on client side MediaMetadataCompat for some reason, but it's in description.artist
        return getString(MetadataKeys.Artist) ?: description.artist
    }

inline var MediaMetadataCompat.Builder.artist: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.Artist, value)
    }


inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(MetadataKeys.AlbumArt)

inline var MediaMetadataCompat.Builder.albumArt: Bitmap?
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putBitmap(MetadataKeys.AlbumArt, value)
    }

inline val MediaMetadataCompat.isSong: Boolean
    get() = getString(MetadataKeys.MediaId).isSongMediaId


inline val MediaMetadataCompat.isLoop: Boolean
    get() = getString(MetadataKeys.MediaId).isLoopMediaId


inline val MediaMetadataCompat.isPlaylist: Boolean
    get() = getString(MetadataKeys.MediaId).isPlaylistMediaId


inline val MediaMetadataCompat.duration: Long
    get() = getLong(MetadataKeys.Duration)

inline var MediaMetadataCompat.Builder.duration: Long
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.Duration, value)
    }


inline val MediaMetadataCompat.startTime: Long
    get() {
        val res = getLong(MetadataKeys.StartTime)
        return if (res == 0L) 0 else res
    }

inline var MediaMetadataCompat.Builder.startTime: Long
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.StartTime, value)
    }


inline val MediaMetadataCompat.endTime: Long
    get() {
        val res = getLong(MetadataKeys.EndTime)
        return if (res == 0L) duration else res
    }

inline var MediaMetadataCompat.Builder.endTime: Long
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.EndTime, value)
    }


inline val MediaMetadataCompat.path: File
    get() = File(getString(MetadataKeys.MediaUri))

inline var MediaMetadataCompat.Builder.path: File
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.MediaUri, value.absolutePath)
    }


fun MediaMetadataCompat.toMediaBrowserMediaItem(): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(toMediaDescriptionCompat(), 0)
}

fun MediaMetadataCompat.toMediaDescriptionCompat(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder().let { desc ->
        desc.setMediaId(mediaId)
        desc.title = title
        desc.artist = artist
        desc.iconBitmap = albumArt
        desc.setExtras(
            path,
            duration,
            startTime,
            endTime
        )

        return@let desc.build()
    }
}

fun MediaMetadataCompat.toExoMediaItem(): com.google.android.exoplayer2.MediaItem {
    return MediaItem.Builder().apply {
        setMediaId(mediaId)
        setUri(Uri.parse(path.absolutePath))

        setMediaMetadata(toExoMediaItemMetadata())
    }.build()
}


fun MediaMetadataCompat.toExoMediaItemMetadata(): com.google.android.exoplayer2.MediaMetadata {
    return com.google.android.exoplayer2.MediaMetadata.Builder().apply {
        setTitle(title)
        setArtist(artist)

        val bundle = Bundle()
        bundle.putLong(MetadataKeys.Duration, duration)
        if (isLoop) {
            bundle.putLong(MetadataKeys.StartTime, startTime)
            bundle.putLong(MetadataKeys.EndTime, endTime)
        }
        setExtras(bundle)
    }.build()
}