package com.daton.tachyonmusic.media.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat

inline val MediaBrowserCompat.MediaItem.artist: String
    get() = description.subtitle!! as String

inline val MediaBrowserCompat.MediaItem.title: String
    get() = description.title!! as String

inline val MediaBrowserCompat.MediaItem.albumArt: Bitmap?
    get() = description.iconBitmap
