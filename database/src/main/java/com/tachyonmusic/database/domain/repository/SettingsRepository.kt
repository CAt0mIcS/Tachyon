package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.database.domain.model.SettingsEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): SettingsEntity
    suspend fun setSettings(settings: SettingsEntity): SettingsEntity

    fun observe(): Flow<SettingsEntity>

    suspend fun removeExcludedFilesRange(toRemove: List<String>)
    suspend fun addExcludedFilesRange(toAdd: List<String>)
    suspend fun setSeekForwardIncrement(intervalMs: Long)
    suspend fun setSeekBackIncrement(intervalMs: Long)
    suspend fun setShouldMillisecondsBeShown(showMilliseconds: Boolean)
}