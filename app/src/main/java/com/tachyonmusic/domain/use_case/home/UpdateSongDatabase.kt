package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.data.repository.StateRepository
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.use_case.library.AssignArtworkToPlayback
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 * Checks if every song in all added directories are also saved in the database and adds missing ones
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val artworkCodex: ArtworkCodex,
    private val assignArtworkToPlayback: AssignArtworkToPlayback,
    private val stateRepository: StateRepository,
    private val log: Logger
) {
    suspend operator fun invoke(settings: SettingsEntity) = withContext(Dispatchers.IO) {
        // TODO: Support more extensions

        val songsToAddToDatabase = fileRepository.getFilesInDirectoriesWithExtensions(
            settings.musicDirectories,
            listOf("mp3")
        ).toMutableList()


        /**
         * Show any songs that are not excluded by [SettingsEntity.excludedSongFiles]
         */
        val songsInRepository = songRepo.getSongs()
        songsInRepository.filter { it.isHidden }.forEach {
            if (!settings.excludedSongFiles.contains(it.mediaId.uri))
                songRepo.updateIsHidden(it.mediaId, false)
        }

        /**
         * Filter songs that are already in database
         */
        val mediaIdsInSongRepository = songsInRepository.map { it.mediaId }
        songsToAddToDatabase.removeAll {
            mediaIdsInSongRepository.contains(MediaId.ofLocalSong(it.uri))
        }

        // TODO: Better async song loading?
        if (songsToAddToDatabase.isNotEmpty()) {
            log.debug("Loading ${songsToAddToDatabase.size} songs...")

            stateRepository.queueLoadingTask("UpdateSongDatabase::loadingNewSongs")

            val songs = mutableListOf<Deferred<SongEntity?>>()
            for (path in songsToAddToDatabase) {
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

            log.debug("Loaded ${songsToAddToDatabase.size} songs")

            // TODO: Warn user of null playback
            songRepo.addAll(songs.awaitAll().filterNotNull())
        }


        /**
         * FIND MISSING ARTWORK
         */
        val songsWithUnknownArtwork =
            songRepo.getSongs().filter { it.artworkType == ArtworkType.UNKNOWN }
        if (songsWithUnknownArtwork.isEmpty())
            stateRepository.finishLoadingTask("UpdateSongDatabase::loadingNewSongs")
        else {
            songsWithUnknownArtwork.map { entity ->
                async {
                    /**
                     * Load new artwork for newly found [entity]
                     */
                    artworkCodex.awaitOrLoad(entity).onEach {
                        val entityToUpdate = it.data?.entityToUpdate
                        if (entityToUpdate != null) {
                            assignArtworkToPlayback(
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
            }.awaitAll()

            stateRepository.finishLoadingTask("UpdateSongDatabase::loadingNewSongs")
        }
    }
}
