package com.tachyonmusic.domain.use_case.player

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.MediaStateHandler

class PlayerListenerHandler(browser: MediaBrowserController) : MediaStateHandler(browser) {

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        Log.d("PlayerListenerHandler", "onIsPlayingChanged with isPlaying = $isPlaying")
    }

    override fun onRegister() {
        // Ensure that state is up to date if there's already a playback playing
        onIsPlayingChanged(browser.isPlaying)
    }
}