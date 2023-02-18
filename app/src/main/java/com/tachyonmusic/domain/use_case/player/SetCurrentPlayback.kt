package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms

class SetCurrentPlayback(private val browser: MediaBrowserController) {
    operator fun invoke(
        playback: SinglePlayback?,
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