package com.tachyonmusic.database.data.data_source

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.util.Duration
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

    @Query("UPDATE SettingsEntity SET ignoreAudioFocus=:value")
    suspend fun setIgnoreAudioFocus(value: Boolean)

    @Query("UPDATE SettingsEntity SET autoDownloadAlbumArtwork=:value")
    suspend fun setAutoDownloadAlbumArtwork(value: Boolean)

    @Query("UPDATE SettingsEntity SET autoDownloadAlbumArtworkWifiOnly=:value")
    suspend fun setAutoDownloadAlbumArtworkWifiOnly(value: Boolean)

    @Query("UPDATE SettingsEntity SET combineDifferentPlaybackTypes=:value")
    suspend fun setCombineDifferentPlaybackTypes(value: Boolean)

    @Query("UPDATE SettingsEntity SET audioUpdateInterval=:value")
    suspend fun setAudioUpdateInterval(value: Duration)

    @Query("UPDATE SettingsEntity SET maxPlaybacksInHistory=:value")
    suspend fun setMaxPlaybacksInHistory(value: Int)

    @Query("UPDATE SettingsEntity SET seekForwardIncrement=:interval")
    suspend fun setSeekForwardIncrement(interval: Duration)

    @Query("UPDATE SettingsEntity SET seekBackIncrement=:interval")
    suspend fun setSeekBackIncrement(interval: Duration)

    @Query("UPDATE SettingsEntity SET animateText=:value")
    suspend fun setAnimateText(value: Boolean)

    @Query("UPDATE SettingsEntity SET shouldMillisecondsBeShown=:value")
    suspend fun setShouldMillisecondsBeShown(value: Boolean)

    @Query("UPDATE SettingsEntity SET playNewlyCreatedCustomizedSong=:value")
    suspend fun setPlayNewlyCreatedCustomizedSong(value: Boolean)

    @Query("UPDATE SettingsEntity SET excludedSongFiles=:excludedFiles")
    suspend fun setExcludedSongFiles(excludedFiles: List<Uri>)

    @Query("UPDATE SettingsEntity SET musicDirectories=:dirs")
    suspend fun setMusicDirectories(dirs: List<Uri>)
}