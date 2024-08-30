package com.tachyonmusic.domain.use_case

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.events.PlaybackNotFoundEvent
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.domain.EventChannel
import com.tachyonmusic.util.replaceWith
import com.tachyonmusic.util.runOnUiThread

enum class PlaybackLocation {
    PREDEFINED_PLAYLIST,
    CUSTOM_PLAYLIST
}

class PlayPlayback(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val addNewPlaybackToHistory: AddNewPlaybackToHistory,
    private val log: Logger,
    private val eventChannel: EventChannel
) {
    @JvmName("invokePlayback")
    suspend operator fun invoke(
        playback: Playback?,
        position: Duration? = null,
        playbackLocation: PlaybackLocation? = null
    ) = runOnUiThread {
        if (playback == null) {
            eventChannel.push(
                PlaybackNotFoundEvent(
                    UiText.StringResource(R.string.invalid_playback, "null"),
                    EventSeverity.Error
                )
            )
            return@runOnUiThread
        }

        if (playbackLocation == PlaybackLocation.CUSTOM_PLAYLIST)
            browser.seekTo(playback.mediaId)
        else
            invokeOnNewPlaylist(playback, position)
        browser.play()
    }

    @JvmName("invokePlaylist")
    suspend operator fun invoke(
        playlist: Playlist?,
        position: Duration? = null
    ) {
        if (playlist == null)
            return

        log.info("Setting playlist to ${playlist.mediaId}")
        browser.setPlaylist(playlist, position)
        browser.prepare()
        addNewPlaybackToHistory(playlist.current)
        browser.play()
    }


    private suspend fun invokeOnNewPlaylist(playback: Playback, position: Duration?) {
        val playlist = getPlaylistForPlayback(playback) ?: return
        invoke(
            playlist.copy(
                playbacks = playlist.playbacks.toMutableList()
                    .replaceWith(playback) { it.mediaId == playback.mediaId }), position
        )
    }
}