package com.tachyonmusic.domain.use_case.main

import android.os.Environment
import com.daton.database.domain.repository.SettingsRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val settingsRepo: SettingsRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        // TODO: Should load this somewhere else
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/").listFiles()!!
        val paths = mutableListOf<String>()
        for (file in files) {
            // TODO: Support more extensions
            if (file.extension == "mp3") {
                paths += file.absolutePath
            }
        }


        val settings = settingsRepo.getSettings()
        songRepo.removeIf {
            // TODO: Shouldn't use LocalSongImpl here!
            if (it is LocalSongImpl) {
                paths.remove(it.path.absolutePath)
                settings.excludedSongFiles.contains(it.path.absolutePath) ||
                        !it.path.exists() || !it.path.isFile
            } else TODO("It is not LocalSongImpl")
        }

        // TODO: Shouldn't use LocalSongImpl here!
        if (paths.isNotEmpty())
            songRepo.addAll(paths.map { LocalSongImpl.build(File(it)) })
    }
}