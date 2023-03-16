package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Either playback has already started but is paused. In this case we just want to start it again
 */
class PlayRecentlyPlayed(
    private val browser: MediaBrowserController,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val playPlayback: PlayPlayback
) {
    suspend operator fun invoke(playback: SinglePlayback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val recentlyPlayedInfo = getRecentlyPlayed()

        runOnUiThread {
            val prevTime = browser.currentPosition ?: recentlyPlayedInfo?.position
            playPlayback(playback, prevTime)
        }
    }
}