package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): SettingsEntity
    suspend fun setSettings(settings: SettingsEntity): SettingsEntity

    fun observe(): Flow<SettingsEntity>

    suspend fun removeExcludedFilesRange(toRemove: List<String>)
    suspend fun addExcludedFilesRange(toAdd: List<String>)
    suspend fun setSeekForwardIncrement(interval: Duration)
    suspend fun setSeekBackIncrement(interval: Duration)
    suspend fun setShouldMillisecondsBeShown(showMilliseconds: Boolean)
}