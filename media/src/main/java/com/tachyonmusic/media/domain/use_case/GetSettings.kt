package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.database.domain.repository.SettingsRepository

class GetSettings(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() = settingsRepository.getSettings()
}