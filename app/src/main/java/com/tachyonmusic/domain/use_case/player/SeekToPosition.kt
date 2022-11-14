package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class SeekToPosition(private val browser: MediaBrowserController) {
    operator fun invoke(position: Long) {
        browser.seekTo(position)
    }
}