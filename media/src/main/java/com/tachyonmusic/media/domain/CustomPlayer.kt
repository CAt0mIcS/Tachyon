package com.tachyonmusic.media.domain

import androidx.media3.common.Player
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController


interface CustomPlayer : Player {
    fun updateTimingData(newTimingData: TimingDataController)

    /**
     * @return the index of [mediaId] in the current playlist or -1 if it doesn't have the playback
     */
    fun indexOfMediaItem(mediaId: MediaId): Int

    interface Listener {
        fun onTimingDataUpdated(controller: TimingDataController?) {}
    }
}