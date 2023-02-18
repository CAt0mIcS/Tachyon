package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.domain.repository.MediaBrowserController

class SetRepeatMode(private val browser: MediaBrowserController) {
    operator fun invoke(repeatMode: RepeatMode) {
        browser.repeatMode = repeatMode
    }
}