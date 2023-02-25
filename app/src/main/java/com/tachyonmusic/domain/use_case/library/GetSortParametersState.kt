package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.domain.repository.MediaBrowserController

class GetSortParametersState(private val browser: MediaBrowserController) {
    operator fun invoke() = browser.sortParamsState
}