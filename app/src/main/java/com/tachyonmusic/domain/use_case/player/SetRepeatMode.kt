package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetRepeatMode(
    private val browser: MediaBrowserController,
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(repeatMode: RepeatMode?) {
        if (repeatMode == null)
            return

        runOnUiThread {
            browser.repeatMode = repeatMode

            withContext(Dispatchers.IO) {
                dataRepository.update(repeatMode = repeatMode)
            }
        }
    }
}