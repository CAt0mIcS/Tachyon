package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController

class SetCurrentPlayback(private val browser: MediaBrowserController) {
    operator fun invoke(playback: Playback?, playWhenReady: Boolean = true): Boolean {
        if (playback == browser.playback)
            return false

        val prevTime = browser.currentPosition ?: 0L
        browser.playWhenReady = playWhenReady
        browser.playback = playback
        browser.seekTo(prevTime)
        return true
    }
}