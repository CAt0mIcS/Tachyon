package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.Duration

class SeekToPosition(private val browser: MediaBrowserController) {
    operator fun invoke(position: Duration) {
        /**
         * Seeking before beginning causes position update with negative values
         * Seeking after end doesn't cause any unwanted side effects
         */
        browser.seekTo(if (position < 0.ms) 0.ms else position)
    }
}