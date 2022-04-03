package com.de.common.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.Nullable

/**
 * Contains extensions for [MediaMetadataCompat]
 */

inline val MediaMetadataCompat.duration: Int
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()

inline var MediaMetadataCompat.Builder.duration: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Int) {
        putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value.toLong())
    }


inline val MediaMetadataCompat.title: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline var MediaMetadataCompat.Builder.title: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: String) {
        putString(MediaMetadataCompat.METADATA_KEY_TITLE, value)
    }


inline val MediaMetadataCompat.artist: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

inline var MediaMetadataCompat.Builder.artist: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: String) {
        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, value)
    }


inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)

inline var MediaMetadataCompat.Builder.albumArt: Bitmap
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Bitmap) {
        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, value)
    }


inline val MediaMetadataCompat.startPos: Int
    get() = getLong("com.de.mucify.START_POS").toInt()

inline var MediaMetadataCompat.Builder.startPos: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Int) {
        putLong("com.de.mucify.START_POS", value.toLong())
    }


inline val MediaMetadataCompat.endPos: Int
    get() = getLong("com.de.mucify.END_POS").toInt()

inline var MediaMetadataCompat.Builder.endPos: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Int) {
        putLong("com.de.mucify.END_POS", value.toLong())
    }


inline val MediaMetadataCompat.mediaId: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline var MediaMetadataCompat.Builder.mediaId: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: String) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, value)
    }


inline val MediaMetadataCompat.currentSongMediaId: String
    get() = getString("com.de.mucify.CURRENT_SONG_MEDIA_ID")

inline var MediaMetadataCompat.Builder.currentSongMediaId: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: String) {
        putString("com.de.mucify.CURRENT_SONG_MEDIA_ID", value)
    }


inline val MediaMetadataCompat.playlistName: String
    get() = getString("com.de.mucify.PLAYLIST_NAME")

inline var MediaMetadataCompat.Builder.playlistName: String
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: String) {
        putString("com.de.mucify.PLAYLIST_NAME", value)
    }


inline val MediaMetadataCompat.songCountInPlaylist: Int
    get() = getLong("com.de.mucify.SONG_COUNT_IN_PLAYLIST").toInt()

inline var MediaMetadataCompat.Builder.songCountInPlaylist: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Int) {
        putLong("com.de.mucify.SONG_COUNT_IN_PLAYLIST", value.toLong())
    }


inline val MediaMetadataCompat.totalPlaylistLength: Int
    get() = getLong("com.de.mucify.TOTAL_PLAYLIST_LENGTH").toInt()

inline var MediaMetadataCompat.Builder.totalPlaylistLength: Int
    get() = throw IllegalAccessException("Cannot get from MediaMetadataCompat.Builder")
    set(value: Int) {
        putLong("com.de.mucify.TOTAL_PLAYLIST_LENGTH", value.toLong())
    }