package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.core.SortParameters

class SetSortParameters(private val browser: MediaBrowserController) {
    operator fun invoke(params: SortParameters) {
        browser.sortParams = params
    }
}