package com.tachyonmusic.database.domain.repository

import android.net.Uri
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): SettingsEntity
    suspend fun setSettings(settings: SettingsEntity): SettingsEntity

    fun observe(): Flow<SettingsEntity>

    suspend fun removeExcludedFilesRange(toRemove: List<Uri>)
    suspend fun addExcludedFilesRange(toAdd: List<Uri>)

    suspend fun update(
        ignoreAudioFocus: Boolean? = null,
        autoDownloadAlbumArtwork: Boolean? = null,
        autoDownloadAlbumArtworkWifiOnly: Boolean? = null,
        combineDifferentPlaybackTypes: Boolean? = null,
        dynamicColors: Boolean? = null,
        audioUpdateInterval: Duration? = null,
        maxPlaybacksInHistory: Int? = null,
        seekForwardIncrement: Duration? = null,
        seekBackIncrement: Duration? = null,
        animateText: Boolean? = null,
        shouldMillisecondsBeShown: Boolean? = null,
        playNewlyCreatedCustomizedSong: Boolean? = null,
        excludedSongFiles: List<Uri>? = null,
        musicDirectories: List<Uri>? = null
    )
}