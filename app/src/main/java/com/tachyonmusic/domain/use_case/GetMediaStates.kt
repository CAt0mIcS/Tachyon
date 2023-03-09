package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.domain.repository.MediaBrowserController

class GetMediaStates(
    private val browser: MediaBrowserController,
    private val context: Context
) {
    fun currentPlaylist() = browser.currentPlaylist
    fun playback() = browser.currentPlayback
    fun isPlaying() = browser.isPlaying
}