package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.isPredefined
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.util.Duration

enum class PlaybackLocation {
    PREDEFINED_PLAYLIST,
    CUSTOM_PLAYLIST
}

class PlayPlayback(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val addNewPlaybackToHistory: AddNewPlaybackToHistory,
    private val log: Logger
) {
    suspend operator fun invoke(
        playback: Playback?,
        position: Duration? = null,
        playbackLocation: PlaybackLocation? = null
    ) {
        when (playback) {
            is SinglePlayback -> {
                invokeOnNewPlaylist(playback, position)
//                if (browser.canPrepare) {
//                    browser.prepare()
//                    browser.seekTo(playback.mediaId, position)
//                } else if (!browser.canPrepare) {
//                    if (browser.currentPlaylist.value == null) {
//                        log.info("Browser doesn't have any playlist. Setting a new playlist and preparing the player...")
//                        invokeOnNewPlaylist(playback, position)
//                    } else if (playback == browser.currentPlayback.value) {
//
//                        if (!playbackLocationMatches(playbackLocation)) {
//                            log.info("Current playback would match, but client requested playback location $playbackLocation, reloading playlist...")
//                            invokeOnNewPlaylist(playback, position)
//                        } else {
//                            log.info("Current playback is already set and browser can't prepare anymore, unpausing playback...")
//                            browser.seekTo(playback.mediaId, position)
//                        }
//
//                    } else if (browser.currentPlaylist.value?.hasPlayback(playback) == true) {
//
//                        if (!playbackLocationMatches(playbackLocation)) {
//                            log.info("Current playback would be in current playlist, but client requested playback location $playbackLocation, reloading playlist...")
//                            invokeOnNewPlaylist(playback, position)
//                        } else {
//                            log.info("New playback already contained in playlist, seeking to new playback...")
//                            browser.seekTo(playback.mediaId, position)
//                        }
//
//                    } else {
//                        log.info("Playlist out of date. Setting a new playlist and preparing the player...")
//                        invokeOnNewPlaylist(playback, position)
//                    }
//                } else error("Shouldn't happen")

//                addNewPlaybackToHistory(playback)
            }

            is Playlist -> {
                log.info("Setting playlist to ${playback.mediaId}")
                browser.setPlaylist(playback)
                browser.prepare()
                browser.seekTo(playback.currentPlaylistIndex, position)

                addNewPlaybackToHistory(playback.current)
            }

            null -> return
            else -> TODO("Invalid playback type ${playback::class.java.name}")
        }
        browser.play()
    }

    private suspend fun invokeOnNewPlaylist(playback: SinglePlayback, position: Duration?) {
        val playlist = getPlaylistForPlayback(playback) ?: return
        invoke(playlist, position)
    }

    private fun playbackLocationMatches(playbackLocation: PlaybackLocation?) =
        playbackLocation == null ||
                playbackLocation == PlaybackLocation.PREDEFINED_PLAYLIST && browser.currentPlaylist.value?.isPredefined == true ||
                playbackLocation == PlaybackLocation.CUSTOM_PLAYLIST && browser.currentPlaylist.value?.isPredefined != true
}