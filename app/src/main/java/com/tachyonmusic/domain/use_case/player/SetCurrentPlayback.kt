package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController

class SetCurrentPlayback(private val browser: MediaBrowserController) {
    operator fun invoke(playback: Playback?) {
        val prevTime = browser.currentPosition ?: 0L
        browser.playback = playback
        browser.seekTo(prevTime)
    }
}