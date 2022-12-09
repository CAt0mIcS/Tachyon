package com.daton.database.data.repository.shared_action

import com.daton.database.domain.model.LoopEntity
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop

class ConvertEntityToLoop(
    private val getArtworkForPlayback: GetArtworkForPlayback
) {
    operator fun invoke(loop: LoopEntity): Loop =
        RemoteLoopImpl(
            loop.mediaId,
            loop.mediaId.source.replace(PlaybackType.Loop.Remote().toString(), ""),
            TimingDataController(loop.timingData, loop.currentTimingDataIndex),
            LocalSongImpl(
                loop.mediaId.underlyingMediaId!!,
                loop.title,
                loop.artist,
                loop.duration
            ).apply {
                this.artwork.value = getArtworkForPlayback(loop)
            }
        )
}