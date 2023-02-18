package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.domain.repository.MediaBrowserController

class SetNewTimingData(private val browser: MediaBrowserController) {
    operator fun invoke(controller: TimingDataController?) {
        browser.timingData = controller
    }
}