package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetCurrentPositionNormalized(private val browser: MediaBrowserController) {
    operator fun invoke(): Float {
        val currentPos = browser.currentPosition?.toFloat() ?: return 0f
        val maxPos = browser.duration ?: return 0f
        return currentPos / maxPos
    }
}