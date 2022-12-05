package com.tachyonmusic.domain.use_case.player

import androidx.compose.runtime.mutableStateListOf
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.MediaStateHandler

class HandleLoopState(browser: MediaBrowserController) : MediaStateHandler(browser) {
    val loopState = mutableStateListOf<TimingData>()

    fun onNewTimingData() {
        if (browser.timingData != null) {
            val data = TimingData(0L, browser.duration ?: return)
            loopState.add(data)
            browser.timingData!!.add(data)
        }
    }

    fun onTimingDataValuesChanged(i: Int, startTime: Long, endTime: Long) {
        loopState[i] = TimingData(startTime, endTime)
    }

    fun onSetUpdatedTimingData() {
        browser.timingData = loopState
    }

    override fun onPlaybackTransition(playback: Playback?) {
        loopState.clear()
        loopState.addAll(playback?.timingData?.timingData ?: emptyList())
    }

    override fun onRegister() {
        onPlaybackTransition(browser.playback ?: return)
    }
}