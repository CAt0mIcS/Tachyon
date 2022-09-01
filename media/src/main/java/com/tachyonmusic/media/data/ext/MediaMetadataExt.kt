package com.tachyonmusic.media.data.ext

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback

val MediaMetadata.name: String?
    get() = extras?.getString(MetadataKeys.Name)

val MediaMetadata.duration: Long?
    get() = extras?.getLong(MetadataKeys.Duration)


val MediaMetadata.timingData: ArrayList<TimingData>?
    get() {
        val strArr = extras?.getStringArray(MetadataKeys.TimingData)
        return if (strArr == null) null
        else TimingData.fromStringArray(strArr)
    }

val MediaMetadata.playback: Playback?
    get() {
        // TODO: WHY??!?!??!?!!?
        extras?.classLoader = Playback::class.java.classLoader
        return extras?.getParcelable(MetadataKeys.Playback) as Playback?
    }