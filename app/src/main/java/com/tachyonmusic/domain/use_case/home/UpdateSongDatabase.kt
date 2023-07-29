package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.util.removeFirst
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Checks if every song in all added directories are also saved in the database and adds missing ones
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val artworkCodex: ArtworkCodex,
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
         * If we remove a song from the [SongRepository] items in history or other playbacks that
         * contain the song will automatically exclude it
         */
        songRepo.removeIf { song ->
            val uri = song.mediaId.uri
            if (uri != null) {
                /**
                 * If it can't find the song to remove it means that the file was deleted
                 */
                !paths.removeFirst { it.uri == uri }
            } else TODO("Invalid path null")
        }

        // TODO: Better async song loading?
        if (paths.isNotEmpty()) {
            log.debug("Loading ${paths.size} songs...")
            val songs = mutableListOf<Deferred<SongEntity?>>()
            for (path in paths) {
                songs += async(Dispatchers.IO) {
                    val metadata = metadataExtractor.loadMetadata(
                        path.uri,
                        path.name ?: "Unknown Title"
                    )

                    if (metadata == null)
                        null
                    else {
                        SongEntity(
                            MediaId.ofLocalSong(path.uri),
                            metadata.title,
                            metadata.artist,
                            metadata.duration
                        )
                    }
                }
            }

            log.debug("Loaded ${paths.size} songs")

            // TODO: Warn user of null playback
            songRepo.addAll(songs.awaitAll().filterNotNull())
        }


        /**
         * FIND MISSING ARTWORK
         */
        songRepo.getSongs().filter { it.artworkType == ArtworkType.UNKNOWN }.forEach { entity ->
            launch {
                /**
                 * Load new artwork for newly found [entity]
                 */
                artworkCodex.awaitOrLoad(entity).onEach {
                    val entityToUpdate = it.data?.entityToUpdate
                    if (entityToUpdate != null) {
                        songRepo.updateArtwork(
                            entityToUpdate.mediaId,
                            entityToUpdate.artworkType,
                            entityToUpdate.artworkUrl
                        )
                    }

                    log.warning(
                        prefix = "ArtworkLoader error on ${entityToUpdate?.title} - ${entityToUpdate?.artist}: ",
                        message = it.message ?: return@onEach
                    )
                }.collect()
            }
        }
    }
}
