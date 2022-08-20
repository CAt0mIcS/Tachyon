package com.tachyonmusic.media.ext

import android.os.Bundle
import com.tachyonmusic.media.data.MetadataKeys
import com.tachyonmusic.media.playback.SinglePlayback
import com.google.android.exoplayer2.MediaMetadata

val MediaMetadata.duration: Long
    get() = extras!!.getLong(MetadataKeys.Duration)

val MediaMetadata.startTime: Long
    get() = extras!!.getLong(MetadataKeys.StartTime)

val MediaMetadata.endTime: Long
    get() = extras!!.getLong(MetadataKeys.EndTime)

val MediaMetadata.playback: SinglePlayback
    get() = extras!!.getParcelable(MetadataKeys.Playback)!!

fun MediaMetadata.Builder.setExtras(
    duration: Long,
    startTime: Long,
    endTime: Long,
    playback: SinglePlayback
) {
    setExtras(Bundle().apply {
        putLong(MetadataKeys.Duration, duration)
        putLong(MetadataKeys.StartTime, startTime)
        putLong(MetadataKeys.EndTime, endTime)
        putParcelable(MetadataKeys.Playback, playback)
    })
}
