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
        val newRange = settings.excludedSongFiles.toMutableList().apply { removeAll(toRemove) }
        settingsDao.updateExcludedSongFiles(newRange)
    }

    override suspend fun addExcludedFilesRange(toAdd: List<String>) {
        val settings = getSettings()
        val newRange = settings.excludedSongFiles.toMutableSet().apply { addAll(toAdd) }
        settingsDao.updateExcludedSongFiles(newRange.toList())
    }
}