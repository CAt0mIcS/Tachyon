package com.daton.media.ext

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MetadataKeys
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
    get() = getString(MetadataKeys.Artist)

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


inline val MediaMetadataCompat.duration: Long
    get() = getLong(MetadataKeys.Duration)

inline var MediaMetadataCompat.Builder.duration: Long
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(MetadataKeys.Duration, value)
    }

inline val MediaMetadataCompat.path: File
    get() = File(getString(MetadataKeys.MediaUri))

inline var MediaMetadataCompat.Builder.path: File
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(MetadataKeys.MediaUri, value.absolutePath)
    }
