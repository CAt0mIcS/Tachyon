package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.runOnUiThread

class SetRepeatMode(
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(repeatMode: RepeatMode?) {
        if (repeatMode == null)
            return

        runOnUiThread {
            browser.setRepeatMode(repeatMode)
        }
    }
}