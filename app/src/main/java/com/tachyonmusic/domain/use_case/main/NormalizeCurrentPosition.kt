package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.normalize

class NormalizeCurrentPosition(private val browser: MediaBrowserController) {
    operator fun invoke(): Float? {
        return browser.currentPosition?.normalize(
            browser.currentPlayback.value?.duration ?: return null
        )
    }
}