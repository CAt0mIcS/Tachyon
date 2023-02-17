package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.Duration

class SetCurrentPlayback(private val browser: MediaBrowserController) {
    operator fun invoke(
        playback: Playback?,
        playWhenReady: Boolean = true,
        seekPos: Duration? = null
    ): Boolean {
        if (playback == browser.playback)
            return false

        val prevTime = seekPos ?: browser.currentPosition ?: 0.ms
        browser.playWhenReady = playWhenReady
        browser.playback = playback
        browser.seekTo(prevTime)
        return true
    }
}