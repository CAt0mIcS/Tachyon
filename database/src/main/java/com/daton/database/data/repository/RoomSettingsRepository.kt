package com.daton.database.data.repository

import com.daton.database.data.data_source.SettingsDao
import com.daton.database.domain.model.SettingsEntity
import com.daton.database.domain.repository.SettingsRepository

class RoomSettingsRepository(
    private val dao: SettingsDao
) : SettingsRepository {

    override suspend fun getSettings() = dao.getSettings() ?: setSettings(SettingsEntity())

    override suspend fun setSettings(settings: SettingsEntity): SettingsEntity {
        dao.setSettings(settings)
        return settings
    }

    override suspend fun removeExcludedFilesRange(toRemove: List<String>) {
        val settings = getSettings()
        val newRange = settings.excludedSongFiles.toMutableList().apply { removeAll(toRemove) }
        dao.updateExcludedSongFiles(newRange)
    }

    override suspend fun addExcludedFilesRange(toAdd: List<String>) {
        val settings = getSettings()
        val newRange = settings.excludedSongFiles.toMutableSet().apply { addAll(toAdd) }
        dao.updateExcludedSongFiles(newRange.toList())
    }
}