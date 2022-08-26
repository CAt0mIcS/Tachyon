package com.tachyonmusic.media.ext

import android.os.Bundle
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.model.SinglePlayback
import com.google.android.exoplayer2.MediaMetadata

val MediaMetadata.duration: Long
    get() = extras!!.getLong(com.tachyonmusic.core.constants.MetadataKeys.Duration)

val MediaMetadata.startTime: Long
    get() = extras!!.getLong(com.tachyonmusic.core.constants.MetadataKeys.StartTime)

val MediaMetadata.endTime: Long
    get() = extras!!.getLong(com.tachyonmusic.core.constants.MetadataKeys.EndTime)

val MediaMetadata.playback: com.tachyonmusic.core.domain.model.SinglePlayback
    get() = extras!!.getParcelable(com.tachyonmusic.core.constants.MetadataKeys.Playback)!!

fun MediaMetadata.Builder.setExtras(
    duration: Long,
    startTime: Long,
    endTime: Long,
    playback: com.tachyonmusic.core.domain.model.SinglePlayback
) {
    setExtras(Bundle().apply {
        putLong(com.tachyonmusic.core.constants.MetadataKeys.Duration, duration)
        putLong(com.tachyonmusic.core.constants.MetadataKeys.StartTime, startTime)
        putLong(com.tachyonmusic.core.constants.MetadataKeys.EndTime, endTime)
        putParcelable(com.tachyonmusic.core.constants.MetadataKeys.Playback, playback)
    })
}
