package com.tachyonmusic.media.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import com.tachyonmusic.core.constants.MetadataKeys

/**
 * Useful extensions for [MediaMetadataCompat].
 */
inline val MediaMetadataCompat.mediaId: String
    get() = getString(com.tachyonmusic.core.constants.MetadataKeys.MediaId)

inline var MediaMetadataCompat.Builder.mediaId: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(com.tachyonmusic.core.constants.MetadataKeys.MediaId, value)
    }


inline val MediaMetadataCompat.title: String
    get() = getString(com.tachyonmusic.core.constants.MetadataKeys.Title)

inline var MediaMetadataCompat.Builder.title: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(com.tachyonmusic.core.constants.MetadataKeys.Title, value)
    }


inline val MediaMetadataCompat.artist: String
    get() = getString(com.tachyonmusic.core.constants.MetadataKeys.Artist)

inline var MediaMetadataCompat.Builder.artist: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putString(com.tachyonmusic.core.constants.MetadataKeys.Artist, value)
    }


inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(com.tachyonmusic.core.constants.MetadataKeys.AlbumArt)

inline var MediaMetadataCompat.Builder.albumArt: Bitmap?
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putBitmap(com.tachyonmusic.core.constants.MetadataKeys.AlbumArt, value)
    }


inline val MediaMetadataCompat.duration: Long
    get() = getLong(com.tachyonmusic.core.constants.MetadataKeys.Duration)

inline var MediaMetadataCompat.Builder.duration: Long
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value) {
        putLong(com.tachyonmusic.core.constants.MetadataKeys.Duration, value)
    }

