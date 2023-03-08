package com.tachyonmusic.domain.use_case.main

import android.content.Context
import android.net.Uri
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.removeFirst
import kotlinx.coroutines.*

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val context: Context,
    private val log: Logger
) {
    suspend operator fun invoke(settings: SettingsEntity) = withContext(Dispatchers.IO) {
        // TODO: Support more extensions

        val paths = fileRepository.getFilesInDirectoriesWithExtensions(
            settings.musicDirectories,
            listOf("mp3")
        ).filter { !settings.excludedSongFiles.contains(it.uri) }.toMutableList()

        /**
         * Remove all invalid or excluded paths in the [songRepo]
         * Update [paths] to only contain new songs that we need to add to [songRepo]
         *
         * TODO
         *  Make sure all parts of the UI are updated if we remove a uri permission
         */
        songRepo.removeIf { song ->
            val uri = song.mediaId.uri
            if (uri != null) {
                paths.removeFirst { it.uri == uri }
                settings.excludedSongFiles.contains(uri)
            } else TODO("Invalid path null")
        }

        // TODO: Better async song loading?
        if (paths.isNotEmpty()) {
            log.debug("Loading ${paths.size} songs...")
            val songs = mutableListOf<Deferred<Pair<Uri, SongMetadataExtractor.SongMetadata?>>>()
            for (path in paths) {
                songs += async(Dispatchers.IO) {
                    path.uri to metadataExtractor.loadMetadata(
                        context.contentResolver,
                        path.uri,
                        path.name ?: "Unknown Title"
                    )
                }
            }

            log.debug("Loaded ${paths.size} songs")

            songRepo.addAll(
                songs.awaitAll().map {
                    return@map if (it.second != null)
                        SongEntity(
                            MediaId.ofLocalSong(it.first),
                            it.second!!.title,
                            it.second!!.artist,
                            it.second!!.duration
                        )
                    else null // TODO: Warn user of invalid playback
                }.filterNotNull()
            )
        }
    }
}