package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController

class NormalizePosition(private val browser: MediaBrowserController) {
    operator fun invoke(): Float? {
        return invoke(
            browser.currentPosition ?: return null,
            browser.duration ?: return null
        )
    }


    operator fun invoke(posMs: Long, duration: Long): Float {
        if (duration == 0L)
            return 0f
        return posMs.toFloat() / duration
    }
}