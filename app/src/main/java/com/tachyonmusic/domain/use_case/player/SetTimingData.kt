package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.domain.repository.MediaBrowserController

class SetTimingData(
    private val browser: MediaBrowserController
) {
    operator fun invoke(timingData: TimingDataController?) {
        if(timingData == null)
            return

        browser.currentPlaybackTimingData = timingData
    }
}