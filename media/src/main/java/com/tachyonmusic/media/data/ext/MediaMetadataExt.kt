package com.tachyonmusic.media.data.ext

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.playback.Playback

val MediaMetadata.name: String?
    get() = extras?.getString(MetadataKeys.Name)

val MediaMetadata.playback: Playback?
    get() {
        // TODO: WHY??!?!??!?!!?
        extras?.classLoader = Playback::class.java.classLoader
        return extras?.getParcelable(MetadataKeys.Playback) as Playback?
    }