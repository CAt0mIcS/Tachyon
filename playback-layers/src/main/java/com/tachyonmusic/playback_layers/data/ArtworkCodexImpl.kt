package com.tachyonmusic.playback_layers.data

import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.playback_layers.domain.ArtworkLoader
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class ArtworkCodexImpl internal constructor(
    private val artworkLoader: ArtworkLoader,
    private val log: Logger
) : ArtworkCodex {
    private data class CodexedArtworkData(
        var artwork: Artwork?,
        val job: CompletableJob = Job()
    )

    private val codex = mutableMapOf<MediaId, CodexedArtworkData>()

    override suspend fun awaitOrLoad(
        entity: SongEntity,
        fetchOnline: Boolean
    ): Flow<Resource<ArtworkCodex.ArtworkUpdateData>> {
        val data = synchronized(codex) {
            val data = codex[entity.mediaId]

            // Ensure that if the artwork hasn't been loaded for entity the job is started
            if (data == null)
                codex[entity.mediaId] = CodexedArtworkData(artwork = null)
            data
        }

        if (data == null) {
            log.debug("Requesting artwork for ${entity.title} - ${entity.artist}")

            return flow {
                emit(Resource.Loading())
                emit(internalRequest(entity, fetchOnline))
            }
        } else if (internalAwait(data.job, entity.mediaId)) {
            /**
             * Doesn't require [SinglePlaybackEntity] database update since the other thread that
             * started the loading job will update it
             */
            return flow {
                emit(
                    Resource.Success(
                        ArtworkCodex.ArtworkUpdateData(
                            artwork = codex[entity.mediaId]?.artwork,
                            entityToUpdate = null
                        )
                    )
                )
            }
        }

        log.debug("Artwork already loaded for ${entity.title} - ${entity.artist}")
        return flow {
            emit(
                Resource.Success(
                    ArtworkCodex.ArtworkUpdateData(
                        artwork = codex[entity.mediaId]?.artwork,
                        entityToUpdate = null
                    )
                )
            )
        }
    }

    override suspend fun await(mediaId: MediaId) {
        val data = synchronized(codex) {
            codex[mediaId]
        }
        internalAwait(data?.job, mediaId)
    }

    override fun get(mediaId: MediaId): Artwork? {
        if (codex.containsKey(mediaId))
            return codex[mediaId]!!.artwork
        throw NoSuchElementException("Key $mediaId is missing in the map.")
    }

    override fun getOrNull(mediaId: MediaId) = codex.getOrDefault(mediaId, null)?.artwork

    override suspend fun loadExisting(entity: SongEntity): ArtworkCodex.ArtworkUpdateData {
        assert(entity.artworkType != ArtworkType.UNKNOWN) { "Invalid artwork type UNKNOWN for ArtworkCodex.loadExisting" }

        if (codex.containsKey(entity.mediaId)) {
            await(entity.mediaId)
            return ArtworkCodex.ArtworkUpdateData(codex[entity.mediaId]?.artwork)
        }

        if (!codex.containsKey(entity.mediaId))
            codex[entity.mediaId] = CodexedArtworkData(artwork = null)

        val res = internalRequest(entity, fetchOnline = false).data
        return ArtworkCodex.ArtworkUpdateData(res?.artwork, res?.entityToUpdate)
    }

    override fun isLoaded(mediaId: MediaId) =
        codex.containsKey(mediaId) && codex[mediaId]!!.job.isCompleted


    private suspend fun internalAwait(job: Job?, mediaId: MediaId): Boolean {
        if (job?.isActive == true) {
            log.debug("Waiting for artwork job $mediaId to join...")
            job.join()
            log.debug("Artwork job $mediaId finished")
            return true
        }
        return false
    }

    private suspend fun internalRequest(
        entity: SongEntity,
        fetchOnline: Boolean
    ): Resource<ArtworkCodex.ArtworkUpdateData> {
        val res = artworkLoader.requestLoad(entity, fetchOnline)
        synchronized(codex) {
            codex[entity.mediaId]?.artwork = res.data?.artwork
            codex[entity.mediaId]?.job?.complete()
        }

        return res
    }
}