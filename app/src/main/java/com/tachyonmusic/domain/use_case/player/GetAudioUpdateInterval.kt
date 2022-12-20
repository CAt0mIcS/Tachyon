package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.database.domain.repository.SettingsRepository
import kotlin.time.Duration.Companion.milliseconds

class GetAudioUpdateInterval(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke() =
        settingsRepository.getSettings().audioUpdateInterval.milliseconds
}