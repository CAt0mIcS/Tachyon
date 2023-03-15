package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.removeFirst
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val artworkCodex: ArtworkCodex,
    private val artworkMapperRepository: ArtworkMapperRepository,
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

        /**************************************************************************
         ********** Load and update artwork
         *************************************************************************/
        val entitiesToUpdate = mutableListOf<Deferred<SongEntity?>>()
        songRepo.getSongs().forEach { entity ->
            // TODO: Don't store unplayable EmbeddedArtwork in codex so that it will load once it becomes playable
            entitiesToUpdate += async {
                com.tachyonmusic.util.delay(6000.ms)
                var entityToUpdate: SongEntity? = null
                artworkCodex.awaitOrLoad(entity /*TODO: fetchOnline*/).onEach {
                    if (it is Resource.Success && it.data?.entityToUpdate != null) {
                        val artwork = it.data?.artwork
                        entity.artworkType = ArtworkType.getType(artwork)
                        entity.artworkUrl =
                            if (artwork is RemoteArtwork) artwork.uri.toURL()
                                .toString() else null
                        entityToUpdate = entity
                    }
                }.collect()
                entityToUpdate
            }
        }

        entitiesToUpdate.awaitAll().forEach {
            if (it != null)
                songRepo.updateArtwork(it.mediaId, it.artworkType, it.artworkUrl)
        }

        artworkMapperRepository.triggerSongReload()
    }
}
