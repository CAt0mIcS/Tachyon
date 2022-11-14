package com.tachyonmusic.domain.use_case.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.domain.repository.MediaBrowserController

class PlayerListenerHandler(browser: MediaBrowserController) : MediaStateHandler(browser) {

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
}