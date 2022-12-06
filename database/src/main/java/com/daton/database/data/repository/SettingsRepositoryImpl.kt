package com.daton.database.data.repository

import com.daton.database.data.data_source.SettingsDao
import com.daton.database.domain.model.SettingsEntity
import com.daton.database.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override suspend fun getSettings() = settingsDao.getSettings() ?: SettingsEntity()

    override suspend fun removeExcludedFilesRange(toRemove: List<String>) {
        val settings = getSettings()
        settings.excludedSongFiles.toMutableList().removeAll(toRemove)
        settingsDao.updateExcludedSongFiles(settings.excludedSongFiles)
    }
}