package com.tachyonmusic.media.util

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.Duration

val MediaMetadata.name: String?
    get() = extras?.getString(MetadataKeys.Name)

val MediaMetadata.duration: Duration?
    get() = extras?.getLong(MetadataKeys.Duration)?.ms


var MediaMetadata.timingData: TimingDataController?
    get() = extras?.parcelable(MetadataKeys.TimingData)
    set(value) {
        extras?.putParcelable(MetadataKeys.TimingData, value)
    }


val MediaMetadata.playback: SinglePlayback?
    get() = extras?.parcelable(MetadataKeys.Playback)

val MediaMetadata.associatedPlaylist: Playlist?
    get() = extras?.parcelable(MetadataKeys.AssociatedPlaylist)
