package com.tachyonmusic.domain.use_case.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.presentation.player.PlaybackState

class HandlePlaybackState(
    browser: MediaBrowserController,
    private val millisecondsToReadableString: MillisecondsToReadableString
) : MediaStateHandler(browser) {
    private val _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    override fun onPlaybackTransition(playback: Playback?) {
        _playbackState.value = PlaybackState(
            playback?.title ?: "",
            playback?.artist ?: "",
            playback?.duration ?: 0L,
            millisecondsToReadableString(playback?.duration),
            if (playback is SinglePlayback) playback.artwork else null
        )
    }
}