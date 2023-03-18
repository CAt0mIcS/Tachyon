package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.sort.domain.SortedPlaybackRepository

class GetRepositoryStates(
    private val browser: MediaBrowserController,
    private val sortedPlaybackRepository: SortedPlaybackRepository
) {
    fun currentPlaylist() = browser.currentPlaylist
    fun playback() = browser.currentPlayback
    fun isPlaying() = browser.isPlaying
    fun repeatMode() = browser.repeatMode

    fun sortPrefs() = sortedPlaybackRepository.sortingPreferences
}