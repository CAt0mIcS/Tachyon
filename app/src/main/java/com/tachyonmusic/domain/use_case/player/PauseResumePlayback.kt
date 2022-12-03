package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class PauseResumePlayback(
    private val browser: MediaBrowserController
) {
    enum class Action {
        Pause, Resume
    }

    operator fun invoke(action: Action) {
        if (browser.playback == null)
            return

        when (action) {
            Action.Pause -> browser.pause()
            Action.Resume -> browser.play()
        }
    }
}