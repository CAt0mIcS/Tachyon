package com.tachyonmusic.database.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.database.domain.model.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    /**
     * Selects the first row in the settings table. We can only have one [SettingsEntity] in the table
     */
    @Query("SELECT * FROM SettingsEntity ORDER BY ROWID ASC LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Query("SELECT * FROM SettingsEntity ORDER BY ROWID ASC LIMIT 1")
    fun observe(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSettings(settings: SettingsEntity)

    @Query("UPDATE SettingsEntity SET excludedSongFiles=:excludedFiles")
    suspend fun updateExcludedSongFiles(excludedFiles: List<String>)

    @Query("UPDATE SettingsEntity SET seekForwardIncrementMs=:intervalMs")
    suspend fun setSeekForwardIncrement(intervalMs: Long)

    @Query("UPDATE SettingsEntity SET seekBackIncrementMs=:intervalMs")
    suspend fun setSeekBackIncrement(intervalMs: Long)

    @Query("UPDATE SettingsEntity SET shouldMillisecondsBeShown=:showMilliseconds")
    suspend fun setShouldMillisecondsBeShown(showMilliseconds: Boolean)
}