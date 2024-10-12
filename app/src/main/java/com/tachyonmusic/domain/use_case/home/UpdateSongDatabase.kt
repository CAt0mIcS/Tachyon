package com.tachyonmusic.domain.use_case.home

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.app.R
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.domain.repository.StateRepository
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.use_case.library.AssignArtworkToPlayback
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.playback_layers.domain.events.InvalidPlaylistItemEvent
import com.tachyonmusic.playback_layers.domain.events.PlaybackNotFoundEvent
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.domain.EventChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlin.math.ceil

/**
 * Checks if every song in all added directories are also saved in the database and adds missing ones
 */
class UpdateSongDatabase(
    @ApplicationContext private val context: Context,
    private val songRepo: SongRepository,
    private val fileRepository: FileRepository,
    private val metadataExtractor: SongMetadataExtractor,
    private val artworkCodex: ArtworkCodex,
    private val assignArtworkToPlayback: AssignArtworkToPlayback,
    private val stateRepository: StateRepository,
    private val log: Logger,
    private val eventChannel: EventChannel
) {
    suspend operator fun invoke(settings: SettingsEntity) = withContext(Dispatchers.IO) {
        // TODO: Support more extensions
        stateRepository.queueLoadingTask("UpdateSongDatabase::loadingNewSongs")

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
        // TODO: Is chunked loading faster than creating a lot of async tasks
        if (songsToAddToDatabase.isNotEmpty()) {
            log.debug("Loading ${songsToAddToDatabase.size} songs...")

            val songs = mutableListOf<Deferred<List<SongEntity>>>()
            val chunkSize = ceil(songsToAddToDatabase.size * .05f).toInt()
            for (pathChunks in songsToAddToDatabase.chunked(chunkSize)) {
                songs += async(Dispatchers.IO) {
                    pathChunks.mapNotNull { path ->
                        val newEntity = loadMetadata(path, settings)
                        if (newEntity == null) {
                            eventChannel.push(
                                PlaybackNotFoundEvent(
                                    UiText.StringResource(
                                        R.string.invalid_playback,
                                        path.name ?: "null"
                                    ),
                                    EventSeverity.Warning
                                )
                            )
                        }
                        newEntity
                    }
                }
            }

            // TODO: Warn user of null playback
            songRepo.addAll(songs.awaitAll().flatten())
            log.debug("Loaded ${songsToAddToDatabase.size} songs")
        }


        /**
         * FIND MISSING ARTWORK
         */
        if (settings.autoDownloadAlbumArtwork) {
            val songsWithUnknownArtwork =
                songRepo.getSongs().filter { it.artworkType == ArtworkType.UNKNOWN }
            log.debug("Trying to find artwork for ${songsWithUnknownArtwork.size} unknowns...")
            if (songsWithUnknownArtwork.isNotEmpty()) {
                /**
                 * Load new artwork for newly found [entity]
                 */

                val chunkSize = ceil(songsWithUnknownArtwork.size * .05f).toInt()
                songsWithUnknownArtwork.chunked(chunkSize).map { entityChunk ->
                    async {
                        entityChunk.map { entity ->
                            loadArtworkForEntity(entity) { entityToUpdate ->
                                assignArtworkToPlayback(
                                    entityToUpdate.mediaId,
                                    entityToUpdate.artworkType,
                                    entityToUpdate.artworkUrl
                                )
                            }
                        }
                    }
                }.awaitAll()
            }
        }

        stateRepository.finishLoadingTask("UpdateSongDatabase::loadingNewSongs")
    }

    private suspend fun loadMetadata(path: DocumentFile, settings: SettingsEntity) =
        withContext(Dispatchers.IO) {
            val metadata = metadataExtractor.loadMetadata(path.uri)

            if (metadata == null)
                null
            else {
                val entity = SongEntity(
                    MediaId.ofLocalSong(path.uri),
                    metadata.title ?: path.name ?: context.getString(R.string.unknown_media_title),
                    metadata.artist ?: context.getString(R.string.unknown_media_artist),
                    metadata.duration,
                    album = metadata.album
                )

                if (settings.autoDownloadAlbumArtwork) {
                    loadArtworkForEntity(entity) { toUpdate ->
                        entity.artworkType = toUpdate.artworkType
                        entity.artworkUrl = toUpdate.artworkUrl
                    }
                }
                entity
            }
        }

    private suspend fun loadArtworkForEntity(
        entity: SongEntity,
        onArtworkUpdate: suspend (SongEntity) -> Unit
    ) = withContext(Dispatchers.IO) {
        artworkCodex.awaitOrLoad(entity).onEach {
            val entityToUpdate = it.data?.entityToUpdate
            if (entityToUpdate != null) {
                onArtworkUpdate(entityToUpdate)
            }

            log.warning(
                prefix = "ArtworkLoader error on ${entityToUpdate?.title} - ${entityToUpdate?.artist}: ",
                message = it.message ?: return@onEach
            )
        }.collect()
    }
}
