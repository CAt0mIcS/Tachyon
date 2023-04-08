package com.tachyonmusic.media.domain

import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController


interface CustomPlayer : Player {
    val mediaItems: List<MediaItem>
    val audioSessionId: Int

    fun updateTimingData(newTimingData: TimingDataController)

    fun setAuxEffectInfo(info: AuxEffectInfo)

    /**
     * @return the index of [mediaId] in the current playlist or -1 if it doesn't have the playback
     */
    fun indexOfMediaItem(mediaId: MediaId): Int

    interface Listener {
        fun onTimingDataUpdated(controller: TimingDataController?) {}
    }
}