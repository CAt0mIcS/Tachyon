package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.domain.repository.MediaBrowserController

abstract class MediaStateHandler(protected val browser: MediaBrowserController) :
    MediaBrowserController.EventListener {
    fun register() {
        browser.registerEventListener(this)
    }

    fun unregister() {
        browser.unregisterEventListener(this)
    }
}