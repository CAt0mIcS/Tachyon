package com.tachyonmusic.domain.use_case

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController

class HandleArtworkState(
    browser: MediaBrowserController
) : MediaStateHandler(browser) {
    private val _artwork = mutableStateOf(browser.playback?.artwork?.value)
    val artwork: State<Artwork?> = _artwork

    override fun onPlaybackTransition(playback: Playback?) {
        if (playback !is SinglePlayback) {
            _artwork.value = null
            return
        }

        _artwork.value = playback.artwork.value
    }
}
