package com.tachyonmusic.domain.use_case.home

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.database.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Updates [SettingsEntity.excludedSongFiles] to make sure that all songs in the list exist
 * and that the browser's seek forward/backward increment is up to date with the settings
 */
class UpdateSettingsDatabase(
    private val repository: SettingsRepository,

    @ApplicationContext
    private val context: Context
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val toRemove = mutableListOf<Uri>()
        val settings = repository.getSettings()

        for (excluded in settings.excludedSongFiles) {
            if (!DocumentFile.fromTreeUri(context, excluded)!!.exists())
                toRemove += excluded
        }

        if (toRemove.isNotEmpty())
            repository.removeExcludedFilesRange(toRemove)
    }
}