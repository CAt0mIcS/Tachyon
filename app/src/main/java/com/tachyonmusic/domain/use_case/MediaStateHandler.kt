package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.MediaBrowserController

abstract class MediaStateHandler(protected val browser: MediaBrowserController) :
    MediaBrowserController.EventListener {
    fun register() {
        browser.registerEventListener(this)
        onRegister()
    }

    fun unregister() {
        browser.unregisterEventListener(this)
    }

    protected open fun onRegister() {}
}