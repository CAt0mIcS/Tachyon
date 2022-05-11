package com.daton.media.ext

import com.daton.media.MetadataKeys
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata

inline val MediaItem.isSong: Boolean
    get() = mediaId.contains("Song_")

inline val MediaItem.isLoop: Boolean
    get() = mediaId.contains("Loop_")

inline val MediaItem.isPlaylist: Boolean
    get() = mediaId.contains("Playlist_")


// Returns 0 as default
inline var MediaMetadata.startTime: Long
    get() = extras?.getLong(MetadataKeys.StartTime) ?: 0
    set(value) {
        // Bundle needs to always exist, which is the case as it stores duration
        extras!!.putLong(MetadataKeys.StartTime, value)
    }

inline var MediaMetadata.endTime: Long
    get() {
        val endTime = extras?.getLong(MetadataKeys.EndTime)
        return if (endTime == null || endTime == 0L) duration else endTime
    }
    set(value) {
        // Bundle needs to always exist, which is the case as it stores duration
        extras!!.putLong(MetadataKeys.EndTime, value)
    }

inline val MediaMetadata.duration: Long
    get() = extras!!.getLong(MetadataKeys.Duration)


