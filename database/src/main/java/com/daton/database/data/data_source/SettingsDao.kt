package com.daton.database.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daton.database.domain.model.SettingsEntity

@Dao
interface SettingsDao {

    /**
     * Selects the first row in the settings table. We can only have one [SettingsEntity] in the table
     */
    @Query("SELECT * FROM SettingsEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSettings(settings: SettingsEntity)

    @Query("UPDATE SettingsEntity SET excludedSongFiles=:excludedFiles")
    suspend fun updateExcludedSongFiles(excludedFiles: List<String>)
}