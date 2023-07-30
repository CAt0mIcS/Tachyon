package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.runOnUiThread

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
    ) = runOnUiThread {
        when (playback) {
            is SinglePlayback -> {
                if (playbackLocation == PlaybackLocation.CUSTOM_PLAYLIST)
                    browser.seekTo(playback.mediaId, position)
                else
                    invokeOnNewPlaylist(playback, position)
            }

            is Playlist -> {
                log.info("Setting playlist to ${playback.mediaId}")

                if (!playback.playbacks.any { it.isPlayable })
                    TODO("Not a single item in playlist $playback is playable")

                while (playback.current?.isPlayable != true) {
                    if (playback.currentPlaylistIndex + 1 >= playback.playbacks.size)
                        playback.currentPlaylistIndex = 0
                    else
                        playback.currentPlaylistIndex += 1
                }

                browser.setPlaylist(playback, position)
                browser.prepare()

                addNewPlaybackToHistory(playback.current)
            }

            null -> return@runOnUiThread
            else -> TODO("Invalid playback type ${playback::class.java.name}")
        }
        browser.play()
    }

    private suspend fun invokeOnNewPlaylist(playback: SinglePlayback, position: Duration?) {
        val playlist = getPlaylistForPlayback(playback) ?: return
        invoke(playlist, position)
    }
}