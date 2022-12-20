package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.repository.SettingsRepository
import java.io.File

/**
 * Updates [SettingsEntity.excludedSongFiles] to make sure that all songs in the list exist
 */
class UpdateSettingsDatabase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        val toRemove = mutableListOf<String>()

        for(excluded in repository.getSettings().excludedSongFiles) {
            if(!File(excluded).exists())
                toRemove += excluded
        }

        if(toRemove.isNotEmpty())
            repository.removeExcludedFilesRange(toRemove)
    }
}