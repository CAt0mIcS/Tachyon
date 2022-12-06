package com.daton.database.domain.repository

import com.daton.database.domain.model.SettingsEntity

interface SettingsRepository {
    suspend fun getSettings(): SettingsEntity
}