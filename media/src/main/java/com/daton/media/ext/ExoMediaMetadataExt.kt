package com.daton.media.ext

import android.os.Bundle
import com.daton.media.data.MetadataKeys
import com.daton.media.device.Playback
import com.google.android.exoplayer2.MediaMetadata

val MediaMetadata.duration: Long
    get() = extras!!.getLong(MetadataKeys.Duration)

val MediaMetadata.startTime: Long
    get() = extras!!.getLong(MetadataKeys.StartTime)

val MediaMetadata.endTime: Long
    get() = extras!!.getLong(MetadataKeys.EndTime)

val MediaMetadata.playback: Playback
    get() = extras!!.getParcelable(MetadataKeys.Playback)!!

fun MediaMetadata.Builder.setExtras(
    duration: Long,
    startTime: Long,
    endTime: Long,
    playback: Playback
) {
    setExtras(Bundle().apply {
        putLong(MetadataKeys.Duration, duration)
        putLong(MetadataKeys.StartTime, startTime)
        putLong(MetadataKeys.EndTime, endTime)
        putParcelable(MetadataKeys.Playback, playback)
    })
}
