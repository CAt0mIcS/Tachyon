package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.GetPlaylistForPlayback
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
    private val getPlaylistForPlayback: GetPlaylistForPlayback
) {
    suspend operator fun invoke(playback: SinglePlayback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val recentlyPlayedInfo = getRecentlyPlayed()

        runOnUiThread {
            val prevTime = recentlyPlayedInfo?.position ?: browser.currentPosition ?: 0.ms

            if (browser.canPrepare) {
                browser.seekTo(playback.mediaId, prevTime)
                browser.prepare()
            }
            else if (playback == browser.currentPlayback.value && !browser.canPrepare) {
                browser.play()
            } else {
                browser.setPlaylist(getPlaylistForPlayback(playback))
                browser.prepare()
            }
        }
    }
}