package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Resource

class ItemClicked(
    private val browser: MediaBrowserController
) {
    operator fun invoke(playback: Playback?): Resource<Unit> {
        // TODO: Validate playback and existence

        browser.playWhenReady = true
        browser.playback = playback

        return Resource.Success()
    }
}