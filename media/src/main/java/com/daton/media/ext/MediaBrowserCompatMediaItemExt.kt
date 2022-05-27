package com.daton.media.ext

import android.support.v4.media.MediaBrowserCompat
import java.io.File

inline val MediaBrowserCompat.MediaItem.artist: String
    get() = description.artist

inline val MediaBrowserCompat.MediaItem.title: String
    get() = description.title as String

inline val MediaBrowserCompat.MediaItem.duration: Long
    get() = description.duration

inline val MediaBrowserCompat.MediaItem.path: File
    get() = description.path

inline val MediaBrowserCompat.MediaItem.startTime: Long
    get() = description.startTime

inline val MediaBrowserCompat.MediaItem.endTime: Long
    get() = description.endTime


