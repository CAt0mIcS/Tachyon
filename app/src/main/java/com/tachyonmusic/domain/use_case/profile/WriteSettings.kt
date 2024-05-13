package com.tachyonmusic.domain.use_case.profile

import android.net.Uri
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.util.Duration
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
        playNewlyCreatedCustomizedSong: Boolean? = null,
        excludedSongFiles: List<Uri>? = null,
        musicDirectories: List<Uri>? = null
    ) = withContext(Dispatchers.IO) {
        // TODO: Validate correct settings

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
            playNewlyCreatedCustomizedSong,
            excludedSongFiles = excludedSongFiles,
            musicDirectories = musicDirectories
        )
    }
}