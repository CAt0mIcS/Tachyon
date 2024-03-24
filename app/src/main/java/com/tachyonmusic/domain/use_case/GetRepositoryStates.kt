package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.domain.PlaybackRepository

class GetRepositoryStates(
    private val browser: MediaBrowserController,
    private val playbackRepository: PlaybackRepository
) {
    fun currentPlaylist() = browser.currentPlaylist
    fun playback() = browser.currentPlayback
    fun isPlaying() = browser.isPlaying
    fun repeatMode() = browser.repeatMode

    fun sortPrefs() = playbackRepository.sortingPreferences
}