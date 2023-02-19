package com.tachyonmusic.domain.use_case.main

import android.net.Uri
import android.os.Environment
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.File
import com.tachyonmusic.util.removeFirst
import kotlinx.coroutines.*

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val settingsRepo: SettingsRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val log: Logger = LoggerImpl()
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val settings = settingsRepo.getSettings()

        // TODO: Shouldn't hard-code path
        // TODO: Support more extensions
        val paths = fileRepository.getFilesInDirectoryWithExtensions(
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/"),
            listOf("mp3")
        ).filter { !settings.excludedSongFiles.contains(it.absolutePath) }.toMutableList()

        /**
         * Remove all invalid or excluded paths in the [songRepo]
         * Update [paths] to only contain new songs that we need to add to [songRepo]
         */
        songRepo.removeIf { song ->
            val path = song.mediaId.path
            if (path != null) {
                paths.removeFirst { it.absolutePath == path.absolutePath }
                val contains = settings.excludedSongFiles.contains(path.absolutePath)
                val notIsFile = !path.isFile
                contains || notIsFile
            } else TODO("Invalid path null")
        }

        // TODO: Better async song loading?
        if (paths.isNotEmpty()) {
            log.debug("Loading ${paths.size} songs...")
            val songs = mutableListOf<Deferred<SongMetadataExtractor.SongMetadata?>>()
            for (path in paths) {
                songs += async(Dispatchers.IO) {
                    metadataExtractor.loadMetadata(Uri.fromFile(path.raw))
                }
            }

            log.debug("Loaded ${paths.size} songs")

            songRepo.addAll(
                songs.awaitAll().map {
                    return@map if (it != null)
                        SongEntity(
                            MediaId.ofLocalSong(File(it.uri.path!!)),
                            it.title,
                            it.artist,
                            it.duration
                        )
                    else null // TODO: Warn user of invalid playback
                }.filterNotNull()
            )
        }
    }
}