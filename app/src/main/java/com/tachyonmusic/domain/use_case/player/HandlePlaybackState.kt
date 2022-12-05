package com.tachyonmusic.domain.use_case.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.MediaStateHandler
import com.tachyonmusic.presentation.player.data.PlaybackState

class HandlePlaybackState(
    browser: MediaBrowserController,
) : MediaStateHandler(browser) {
    private val _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    override fun onPlaybackTransition(playback: Playback?) {
        _playbackState.value = PlaybackState(
            playback?.title ?: "",
            playback?.artist ?: "",
            playback?.duration ?: 0L,
            if (playback is SinglePlayback) listOf(playback) else emptyList()
        )
    }

    override fun onRegister() {
        // Ensure that state is up to date if there's already a playback playing
        onPlaybackTransition(browser.playback)
    }
}