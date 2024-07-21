package com.tachyonmusic.media.domain

import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController


@UnstableApi
interface CustomPlayer : Player {
    var audioSessionId: Int

    fun updateTimingData(newTimingData: TimingDataController)
    fun seekToTimingDataIndex(i: Int)
    fun setPlayer(player: Player)

    fun setAuxEffectInfo(info: AuxEffectInfo)

    /**
     * @return the index of [mediaId] in the current playlist or -1 if it doesn't have the playback
     */
    fun indexOfMediaItem(mediaId: MediaId): Int

    interface Listener {
        fun onTimingDataUpdated(controller: TimingDataController?) {}
    }
}