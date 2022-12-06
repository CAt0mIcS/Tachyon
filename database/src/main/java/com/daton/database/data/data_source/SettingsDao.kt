package com.daton.database.data.data_source

import androidx.room.Dao
import androidx.room.Query
import com.daton.database.domain.model.SettingsEntity

@Dao
interface SettingsDao {

    /**
     * Selects the first row in the settings table. We can only have one [SettingsEntity] in the table
     */
    @Query("SELECT * FROM settingsEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getSettings(): SettingsEntity?
}