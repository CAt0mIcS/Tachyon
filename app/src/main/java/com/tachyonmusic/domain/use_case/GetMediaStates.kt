package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetMediaStates(private val browser: MediaBrowserController) {
    fun associatedPlaylist() = browser.associatedPlaylistState
    fun playback() = browser.playbackState
    fun playWhenReady() = browser.playWhenReadyState
    fun timingData() = browser.timingDataState
    fun sortParameters() = browser.sortParamsState
    fun repeatMode() = browser.repeatModeState
}