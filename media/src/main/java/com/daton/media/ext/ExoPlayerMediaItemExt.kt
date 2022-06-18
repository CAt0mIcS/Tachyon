package com.daton.media.ext

import android.support.v4.media.MediaMetadataCompat
import com.daton.media.data.MediaId
import com.daton.media.data.MetadataKeys
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata

inline val MediaItem.isSong: Boolean
    get() = mediaId.toMediaId().isSong

inline val MediaItem.isLoop: Boolean
    get() = mediaId.toMediaId().isLoop

inline val MediaItem.isPlaylist: Boolean
    get() = mediaId.toMediaId().isPlaylist


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

fun MediaMetadata.toMediaMetadataCompat(mediaId: MediaId): MediaMetadataCompat =
    MediaMetadataCompat.Builder().let {
        it.title = title?.toString()
        it.artist = artist?.toString()
        it.duration = duration

//        TODO(it.albumArt = artworkData)
        it.startTime = startTime
        it.endTime = endTime
        it.mediaId = mediaId

        return@let it.build()
    }


