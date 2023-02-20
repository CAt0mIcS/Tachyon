package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayRecentlyPlayed(
    private val browser: MediaBrowserController,
    private val getRecentlyPlayed: GetRecentlyPlayed,
) {
    suspend operator fun invoke(playback: SinglePlayback?) = withContext(Dispatchers.IO) {
        val recentlyPlayedInfo = getRecentlyPlayed()

        runOnUiThread {
            if (playback == null)
                return@runOnUiThread
            val prevTime = recentlyPlayedInfo?.position ?: browser.currentPosition ?: 0.ms

            if (playback == browser.playback) {
                if (!browser.playWhenReady) {
                    browser.play()
                }
                browser.seekTo(prevTime)
                browser.prepare()
                return@runOnUiThread
            }

            browser.playback = playback
            browser.playWhenReady = true
            browser.seekTo(prevTime)
        }
    }
}