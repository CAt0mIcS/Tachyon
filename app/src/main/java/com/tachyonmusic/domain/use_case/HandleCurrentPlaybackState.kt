package com.tachyonmusic.domain.use_case

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController

class HandleCurrentPlaybackState(
    browser: MediaBrowserController
) : MediaStateHandler(browser) {
    private var _currentPlayback = mutableStateOf(browser.playback)
    val currentPlayback: State<Playback?> = _currentPlayback

    override fun onPlaybackTransition(playback: Playback?) {
        _currentPlayback.value = playback
    }
}
