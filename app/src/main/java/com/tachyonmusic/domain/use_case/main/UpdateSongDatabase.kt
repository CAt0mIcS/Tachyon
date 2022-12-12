package com.tachyonmusic.domain.use_case.main

import android.os.Environment
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SettingsRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.SongMetadata
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val settingsRepo: SettingsRepository,
    private val fileRepository: FileRepository,
    private val log: Logger = Log()
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {

        // TODO: Shouldn't hard-code path
        // TODO: Support more extensions
        val paths = fileRepository.getFilesInDirectoryWithExtensions(
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/"),
            listOf("mp3")
        ).toMutableList()

        val settings = settingsRepo.getSettings()
        songRepo.removeIf {
            val path = it.mediaId.path
            if (path != null) {
                paths.remove(path)
                settings.excludedSongFiles.contains(path.absolutePath) ||
                        !path.exists() || !path.isFile
            } else TODO("Invalid path null")

        }

        // TODO: Better async song loading?
        if (paths.isNotEmpty()) {
            log.debug("Loading ${paths.size} songs...")
            val songs = mutableListOf<Deferred<SongMetadata>>()
            for (path in paths) {
                songs += async(Dispatchers.IO) {
                    SongMetadata(path)
                }
            }

            log.debug("Loaded ${paths.size} songs")

            songRepo.addAll(
                songs.awaitAll().map {
                    SongEntity(
                        MediaId.ofLocalSong(it.path),
                        it.title,
                        it.artist,
                        it.duration
                    )
                })
        }
    }
}