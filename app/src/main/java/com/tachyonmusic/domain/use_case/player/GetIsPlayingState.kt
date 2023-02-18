package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetIsPlayingState(private val browser: MediaBrowserController) {
    operator fun invoke() = browser.playWhenReadyState
}