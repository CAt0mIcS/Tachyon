package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class SeekPosition(private val browser: MediaBrowserController) {
    operator fun invoke(position: Long) {
        /**
         * Seeking before beginning causes position update with negative values
         * Seeking after end doesn't cause any unwanted side effects
         */
        browser.seekTo(if (position < 0L) 0L else position)
    }
}