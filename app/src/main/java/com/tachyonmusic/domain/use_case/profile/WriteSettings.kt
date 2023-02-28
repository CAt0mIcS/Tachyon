package com.tachyonmusic.domain.use_case.profile

import android.net.Uri
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WriteSettings(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        ignoreAudioFocus: Boolean? = null,
        autoDownloadAlbumArtwork: Boolean? = null,
        autoDownloadAlbumArtworkWifiOnly: Boolean? = null,
        combineDifferentPlaybackTypes: Boolean? = null,
        audioUpdateInterval: Duration? = null,
        maxPlaybacksInHistory: Int? = null,
        seekForwardIncrement: Duration? = null,
        seekBackIncrement: Duration? = null,
        animateText: Boolean? = null,
        shouldMillisecondsBeShown: Boolean? = null,
        excludedSongFiles: List<Uri>? = null,
        musicDirectories: List<Uri>? = null
    ) = withContext(Dispatchers.IO) {
        settingsRepository.update(
            ignoreAudioFocus,
            autoDownloadAlbumArtwork,
            autoDownloadAlbumArtworkWifiOnly,
            combineDifferentPlaybackTypes,
            audioUpdateInterval,
            maxPlaybacksInHistory,
            seekForwardIncrement,
            seekBackIncrement,
            animateText,
            shouldMillisecondsBeShown,
            excludedSongFiles,
            musicDirectories
        )
    }
}