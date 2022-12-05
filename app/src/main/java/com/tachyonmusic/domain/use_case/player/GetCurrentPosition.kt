package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetCurrentPosition(private val browser: MediaBrowserController) {
    operator fun invoke() = browser.currentPosition ?: 0L
}