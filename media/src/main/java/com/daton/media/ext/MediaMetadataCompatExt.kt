package com.daton.media.ext

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
    get() = getString(MetadataKeys.MediaId).contains("Song_")


inline val MediaMetadataCompat.isLoop: Boolean
    get() = getString(MetadataKeys.MediaId).contains("Loop_")


inline val MediaMetadataCompat.isPlaylist: Boolean
    get() = getString(MetadataKeys.MediaId).contains("Playlist_")


inline val MediaMetadataCompat.duration: Int
    get() = getLong(MetadataKeys.Duration).toInt()

inline var MediaMetadataCompat.Builder.duration: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.Duration, value.toLong())
    }


inline val MediaMetadataCompat.startTime: Int?
    get() {
        val res = getLong(MetadataKeys.StartTime)
        return if (res == 0L) null else res.toInt()
    }

inline var MediaMetadataCompat.Builder.startTime: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.StartTime, value.toLong())
    }


inline val MediaMetadataCompat.endTime: Int?
    get() {
        val res = getLong(MetadataKeys.EndTime)
        return if (res == 0L) null else res.toInt()
    }

inline var MediaMetadataCompat.Builder.endTime: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.EndTime, value.toLong())
    }


inline val MediaMetadataCompat.path: File
    get() = File(getString(MetadataKeys.MediaUri).toString())

inline var MediaMetadataCompat.Builder.path: File
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.MediaUri, value.absolutePath)
    }


fun MediaMetadataCompat.toMediaItem(): com.google.android.exoplayer2.MediaItem {
    return MediaItem.Builder().apply {
        setMediaId(mediaId)
        setUri(Uri.parse(path.absolutePath))

        setMediaMetadata(toMediaItemMetadata())
    }.build()
}


fun MediaMetadataCompat.toMediaItemMetadata(): com.google.android.exoplayer2.MediaMetadata {
    return com.google.android.exoplayer2.MediaMetadata.Builder().apply {
        setTitle(title)
        setArtist(artist)

        val bundle = Bundle()
        bundle.putInt(MetadataKeys.Duration, duration)
        if (isLoop) {
            bundle.putInt(MetadataKeys.StartTime, startTime!!)
            bundle.putInt(MetadataKeys.EndTime, endTime!!)
        }
        setExtras(bundle)
    }.build()
}