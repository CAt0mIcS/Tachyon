package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.database.domain.repository.SettingsRepository

data class SeekIncrements(
    var forward: Long = 0L,
    var backward: Long = 0L
)

class GetSeekIncrements(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): SeekIncrements {
        val settings = repository.getSettings()
        return SeekIncrements(settings.seekForwardIncrementMs, settings.seekBackIncrementMs)
    }
}