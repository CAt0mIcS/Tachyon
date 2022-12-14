package com.daton.database.domain.repository

import com.daton.database.domain.model.SettingsEntity

interface SettingsRepository {
    suspend fun getSettings(): SettingsEntity
    suspend fun setSettings(settings: SettingsEntity): SettingsEntity

    suspend fun removeExcludedFilesRange(toRemove: List<String>)
    suspend fun addExcludedFilesRange(toAdd: List<String>)
}