package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.util.File

/**
 * Updates [SettingsEntity.excludedSongFiles] to make sure that all songs in the list exist
 * and that the browser's seek forward/backward increment is up to date with the settings
 */
class UpdateSettingsDatabase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        val toRemove = mutableListOf<String>()
        val settings = repository.getSettings()

        for (excluded in settings.excludedSongFiles) {
            if (!File(excluded).exists())
                toRemove += excluded
        }

        if (toRemove.isNotEmpty())
            repository.removeExcludedFilesRange(toRemove)
    }
}