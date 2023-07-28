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
            return@withContext

        uriPermissionRepository.addPermissionUri(uri)

        if (uriPermissionRepository.hasPermission(uri))
            settingsRepository.update(musicDirectories = settingsRepository.getSettings().musicDirectories + uri)
    }
}
