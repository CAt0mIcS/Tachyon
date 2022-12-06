package com.daton.database.data.repository

import com.daton.database.data.data_source.SettingsDao
import com.daton.database.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override suspend fun getSettings() = settingsDao.getSettings()
}