package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetCurrentPlaybackState(private val browser: MediaBrowserController) {
    operator fun invoke() = browser.playbackState
}