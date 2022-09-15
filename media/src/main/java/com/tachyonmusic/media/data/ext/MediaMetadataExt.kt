package com.tachyonmusic.media.data.ext

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback

val MediaMetadata.name: String?
    get() = extras?.getString(MetadataKeys.Name)

val MediaMetadata.duration: Long?
    get() = extras?.getLong(MetadataKeys.Duration)


var MediaMetadata.timingData: TimingDataController?
    get() {
        extras?.classLoader = TimingDataController::class.java.classLoader
        return extras?.getParcelable(MetadataKeys.TimingData) as TimingDataController?
    }
    set(value) {
        extras?.putParcelable(MetadataKeys.TimingData, value)
    }


val MediaMetadata.playback: Playback?
    get() {
        // TODO: WHY??!?!??!?!!?
        extras?.classLoader = Playback::class.java.classLoader
        return extras?.getParcelable(MetadataKeys.Playback) as Playback?
    }