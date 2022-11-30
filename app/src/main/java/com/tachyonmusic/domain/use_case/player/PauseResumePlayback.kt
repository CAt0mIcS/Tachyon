package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

class PauseResumePlayback(
    private val browser: MediaBrowserController
) {
    enum class Action {
        Pause, Resume
    }

    operator fun invoke(action: Action) {
        if (action == Action.Pause)
            browser.pause()
        else if (action == Action.Resume)
            browser.play()
        else
            TODO("Invalid action $action")
    }
}