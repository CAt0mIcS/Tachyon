package com.tachyonmusic.permission.domain.model

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.util.Duration

class LoopPermissionEntity(
    mediaId: MediaId,
    songTitle: String,
    songArtist: String,
    songDuration: Duration,
    isPlayable: Boolean,
    val timingData: List<TimingData>,
    val currentTimingDataIndex: Int
) : SinglePlaybackPermissionEntity(mediaId, songTitle, songArtist, songDuration, isPlayable) {

    val name: String
        get() = mediaId.source.replace(PlaybackType.Loop.Remote().toString(), "")
}


internal fun LoopEntity.toPermissionEntity(isPlayable: Boolean) = LoopPermissionEntity(
    mediaId,
    songTitle,
    songArtist,
    songDuration,
    isPlayable,
    timingData,
    currentTimingDataIndex
)