package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.util.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddNewPlaybackToHistory(
    private val repository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val settings = settingsRepository.getSettings()
        if (settings.maxPlaybacksInHistory <= 0)
            return@withContext

        val entity = playback.toEntity()

        repository += entity

        /**
         * Shrinking history to [settings.maxPlaybacksInHistory]
         */
        val history = repository.getHistory()
        if (history.size > settings.maxPlaybacksInHistory) {
            val toRemove =
                history.toMutableList().subList(settings.maxPlaybacksInHistory, history.size)
            repository -= toRemove.map { it.mediaId }
        }
    }
}