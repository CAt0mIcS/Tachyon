package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource

class PlayPlayback(
    private val browser: MediaBrowserController
) {
    operator fun invoke(playback: SinglePlayback?): Resource<Unit> {
        // TODO: Validate playback and existence

        browser.playWhenReady = true
        browser.playback = playback

        return Resource.Success()
    }
}