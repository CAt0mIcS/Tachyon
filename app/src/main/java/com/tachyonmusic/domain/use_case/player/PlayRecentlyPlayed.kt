package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
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
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        val recentlyPlayedInfo = getRecentlyPlayed()

        runOnUiThread {
            if (playback == null)
                return@runOnUiThread

            if (isBrowserPlaybackSet(playback)) {
                if (!browser.playWhenReady)
                    browser.play()
                return@runOnUiThread
            }


            val prevTime = browser.currentPosition ?: recentlyPlayedInfo?.position ?: 0.ms
            browser.playWhenReady = true
            browser.playback = playback
            browser.seekTo(prevTime)
        }
    }

    private fun isBrowserPlaybackSet(playback: Playback) =
        playback == browser.playback && browser.currentPosition != null
}