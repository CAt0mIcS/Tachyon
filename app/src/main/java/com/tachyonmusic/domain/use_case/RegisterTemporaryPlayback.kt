package com.tachyonmusic.domain.use_case

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tachyonmusic.app.R
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.domain.EventChannel
import com.tachyonmusic.util.dyn
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext

/**
 * Registers a new playback to which we only have temporary access. For example, when the user clicks
 * on an audio file in the Android FileManager and chooses to open it with Tachyon we get only
 * temporary access to that URI (until app closes). Because of this, we don't store that song in
 * the database and only store it in memory so that it gets cleaned up automatically when the app closes
 */
class RegisterTemporaryPlayback(
    @ApplicationContext private val context: Context,
    private val metadataExtractor: SongMetadataExtractor,
    private val playbackRepository: PlaybackRepository,
    private val artworkCodex: ArtworkCodex,
    private val log: Logger,
    private val eventChannel: EventChannel,
    private val playPlayback: PlayPlayback
) {
    suspend operator fun invoke(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val metadata = metadataExtractor.loadMetadata(uri) ?: return@withContext false.also {
            eventChannel.push("Failed to load metadata for $uri".dyn, EventSeverity.Error)
        }

        val path = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false.also {
            eventChannel.push("Failed to create DocumentFile from $uri".dyn, EventSeverity.Error)
        }
        if (!path.canRead())
            return@withContext false.also {
                eventChannel.push(
                    "Unable to read temporary playback ${path.name}".dyn,
                    EventSeverity.Error
                )
            }

        val entity = SongEntity(
            MediaId.ofLocalTemporarySong(uri),
            metadata.title ?: path.name ?: context.getString(R.string.unknown_media_title),
            metadata.artist ?: context.getString(R.string.unknown_media_artist),
            metadata.duration,
            album = metadata.album
        )

        loadArtworkForEntity(
            entity,
            fetchOnline = false
        ) { toUpdate ->
            if (toUpdate.artworkType == ArtworkType.EMBEDDED)
                entity.artworkType = ArtworkType.EMBEDDED
        }

        playbackRepository.addTemporaryPlayback(entity)
        playbackRepository.songFlow.transformWhile { songs ->
            emit(songs)
            val res = songs.find { it.mediaId == entity.mediaId } == null
            res
        }.onEach { songs ->
            val playback = songs.find { it.mediaId == entity.mediaId }
            if (playback != null)
                playPlayback(playback)
        }.collect()

        true
    }

    private suspend fun loadArtworkForEntity(
        entity: SongEntity,
        fetchOnline: Boolean,
        onArtworkUpdate: suspend (SongEntity) -> Unit
    ) = withContext(Dispatchers.IO) {
        artworkCodex.awaitOrLoad(entity, fetchOnline).onEach {
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