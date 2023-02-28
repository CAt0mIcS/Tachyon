package com.tachyonmusic.domain.use_case

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.presentation.util.hasUriPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetMusicDirectories(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {
    @JvmName("invokeUris")
    suspend operator fun invoke(uris: List<Uri?>) = withContext(Dispatchers.IO) {
        settingsRepository.update(musicDirectories = uris.toSet().filterNotNull().filter { uri ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            context.hasUriPermission(uri)
        })
    }

    @JvmName("invokeUri")
    suspend operator fun invoke(uri: Uri?) = invoke(listOf(uri))
}