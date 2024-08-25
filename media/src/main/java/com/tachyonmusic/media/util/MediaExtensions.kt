package com.tachyonmusic.media.util

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

val MediaMetadata.duration: Duration?
    get() = extras?.getLong(MetadataKeys.Duration)?.ms


var MediaMetadata.timingData: TimingDataController?
    get() = extras?.getParcelable(MetadataKeys.TimingData)
    set(value) {
        extras?.putParcelable(MetadataKeys.TimingData, value)
    }