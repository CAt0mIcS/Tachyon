package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.util.runOnUiThread

class PlayPlayback(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val log: Logger
) {
    suspend operator fun invoke(
        playback: Playback?,
        sortParams: SortParameters = SortParameters()
    ) = runOnUiThread {
        when (playback) {
            is SinglePlayback -> {
                if (playback == browser.currentPlayback.value && !browser.canPrepare) {
                    log.info("Current playback is already set and browser can't prepare anymore, unpausing playback...")
                    browser.play()
                } else {
                    log.info("Playback out of date. Setting a new playlist and preparing the player...")
                    // TODO: Optimize
                    val playlist =
                        getPlaylistForPlayback(playback, sortParams) ?: return@runOnUiThread
                    browser.setPlaylist(playlist)
                    browser.prepare()
                    browser.seekTo(playlist.currentPlaylistIndex, null)
                    browser.play()
                }
            }

            is Playlist -> {
                browser.setPlaylist(playback)
                browser.prepare()
                browser.seekTo(playback.currentPlaylistIndex, null)
                browser.play()
            }
        }
    }
}