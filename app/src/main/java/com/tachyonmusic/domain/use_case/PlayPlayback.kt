package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.util.Duration

class PlayPlayback(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val log: Logger
) {
    suspend operator fun invoke(
        playback: Playback?,
        position: Duration? = null,
        sortParams: SortParameters = SortParameters()
    ) {
        when (playback) {
            is SinglePlayback -> {
                if (browser.canPrepare) {
                    browser.prepare()
                    browser.seekTo(playback.mediaId, position)
                } else if (!browser.canPrepare) {
                    if (playback == browser.currentPlayback.value) {
                        log.info("Current playback is already set and browser can't prepare anymore, unpausing playback...")
                        browser.seekTo(playback.mediaId, position)
                    } else if (browser.currentPlaylist.value?.hasPlayback(playback) == true) {
                        log.info("New playback already contained in playlist, seeking to new playback...")
                        browser.seekTo(playback.mediaId, position)
                    } else {
                        log.info("Playback out of date. Setting a new playlist and preparing the player...")
                        val playlist = getPlaylistForPlayback(playback, sortParams) ?: return
                        return invoke(playlist, position, sortParams)
                    }
                } else error("Shouldn't happen")
            }

            is Playlist -> {
                log.info("Setting playlist to ${playback.mediaId}")
                browser.setPlaylist(playback)
                browser.prepare()
                browser.seekTo(playback.currentPlaylistIndex, position)
            }

            null -> return
            else -> TODO("Invalid playback type ${playback::class.java.name}")
        }
        browser.play()
    }
}