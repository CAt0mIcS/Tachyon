package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SettingsRepository

class ObserveSettings(
    private val repository: SettingsRepository
) {
    operator fun invoke() = repository.observe()
}