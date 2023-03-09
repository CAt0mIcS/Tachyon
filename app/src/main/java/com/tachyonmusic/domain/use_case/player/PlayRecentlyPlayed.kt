package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
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
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val log: Logger
) {
    suspend operator fun invoke(playback: SinglePlayback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val recentlyPlayedInfo = getRecentlyPlayed()

        runOnUiThread {
            val prevTime = recentlyPlayedInfo?.position ?: browser.currentPosition ?: 0.ms

            if (browser.canPrepare) {
                log.info("Browser can be prepared. Seeking to playback and preparing...")
                browser.prepare()
                browser.seekTo(playback.mediaId, prevTime)
                browser.play()
            } else if (playback == browser.currentPlayback.value && !browser.canPrepare) {
                log.info("Current playback is already set and browser can't prepare anymore, unpausing playback...")
                browser.play()
            } else {
                log.info("Unable to prepare and playback out of date. Setting a new playlist and preparing the player...")
                val playlist = getPlaylistForPlayback(playback) ?: return@runOnUiThread
                browser.setPlaylist(playlist)
                browser.prepare()
                browser.seekTo(playlist.currentPlaylistIndex, prevTime)
                browser.play()
            }
        }
    }
}