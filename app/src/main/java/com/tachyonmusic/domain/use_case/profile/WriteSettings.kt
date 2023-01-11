package com.tachyonmusic.domain.use_case.profile

import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository

class WriteSettings(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: SettingsEntity) = settingsRepository.setSettings(settings)
}