package com.tachyonmusic.domain.use_case

import android.content.Context
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.util.isPlayable
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class GetMediaStates(
    private val browser: MediaBrowserController,
    private val context: Context
) {
    fun associatedPlaylist() = browser.associatedPlaylistState

    fun playback() = browser.playbackState.onEach { pb ->
        pb?.isPlayable?.update { pb.mediaId.uri.isPlayable(context) }
    }

    fun playWhenReady() = browser.playWhenReadyState
    fun timingData() = browser.timingDataState
    fun sortParameters() = browser.sortParamsState
}