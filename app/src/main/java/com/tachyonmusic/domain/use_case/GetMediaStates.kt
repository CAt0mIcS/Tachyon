package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetMediaStates(
    private val browser: MediaBrowserController
) {
    fun currentPlaylist() = browser.currentPlaylist
    fun playback() = browser.currentPlayback
    fun isPlaying() = browser.isPlaying
}