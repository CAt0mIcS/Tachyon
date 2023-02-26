package com.tachyonmusic.domain.use_case.profile

import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WriteSettings(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: SettingsEntity) =
        withContext(Dispatchers.IO) { settingsRepository.setSettings(settings) }
}