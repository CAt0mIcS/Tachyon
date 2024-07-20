package com.tachyonmusic.domain.use_case

import android.net.Uri
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterNewUriPermission(
    private val uriPermissionRepository: UriPermissionRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(uri: Uri?) = withContext(Dispatchers.IO) {
        if (uri == null)
            return@withContext false

        uriPermissionRepository.addPermissionUri(uri)

        if (uriPermissionRepository.hasPermission(uri)) {
            val settings = settingsRepository.getSettings()
            settingsRepository.update(
                excludedSongFiles = settings.excludedSongFiles.toMutableList().apply {
                    /**
                     * Remove all excluded song files in the [uri] folder that was added to permissions
                     */
                    removeAll { uri.toString() in it.toString() }
                },
                musicDirectories = settings.musicDirectories + uri
            )

            true
        } else
            false
    }
}
