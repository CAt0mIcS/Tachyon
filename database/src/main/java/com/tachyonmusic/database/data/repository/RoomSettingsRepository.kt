package com.tachyonmusic.database.data.repository

import com.tachyonmusic.database.data.data_source.SettingsDao
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository

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