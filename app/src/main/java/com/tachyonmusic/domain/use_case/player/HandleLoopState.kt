package com.tachyonmusic.domain.use_case.player

import androidx.compose.runtime.mutableStateListOf
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController

class HandleLoopState(browser: MediaBrowserController) : MediaStateHandler(browser) {
    val loopState = mutableStateListOf<TimingData>()

    override fun onPlaybackTransition(playback: Playback?) {
        loopState.addAll(playback?.timingData?.timingData ?: emptyList())
    }
}