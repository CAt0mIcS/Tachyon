package com.tachyonmusic.media.data.ext

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback

val MediaMetadata.name: String?
    get() = extras?.getString(MetadataKeys.Name)

val MediaMetadata.duration: Long?
    get() = extras?.getLong(MetadataKeys.Duration)


var MediaMetadata.timingData: TimingDataController?
    get() = extras?.parcelable(MetadataKeys.TimingData)
    set(value) {
        extras?.putParcelable(MetadataKeys.TimingData, value)
    }


val MediaMetadata.playback: Playback?
    get() = extras?.parcelable(MetadataKeys.Playback)
