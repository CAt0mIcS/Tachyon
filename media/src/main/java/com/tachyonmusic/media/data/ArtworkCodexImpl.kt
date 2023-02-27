package com.tachyonmusic.media.data

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.media.domain.ArtworkLoader
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.ms
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ArtworkCodexImpl internal constructor(
    private val artworkLoader: ArtworkLoader,
    private val log: Logger
) : ArtworkCodex {
    private data class ArtworkData(
        var artwork: Artwork?,
        val job: CompletableJob = Job()
    )

    private val codex = mutableMapOf<MediaId, ArtworkData>()

    override suspend fun awaitOrLoad(
        entity: SongEntity,
        fetchOnline: Boolean
    ): Flow<Resource<SongEntity?>> {
        val data = synchronized(codex) {
            val data = codex[entity.mediaId]

            // Ensure that if the artwork hasn't been loaded for entity the job is started
            if (data == null)
                codex[entity.mediaId] = ArtworkData(artwork = null)
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
            return flow { emit(Resource.Success(data = null)) }
        }

        log.debug("Artwork already loaded for ${entity.title} - ${entity.artist}")
        return flow { emit(Resource.Success(data = null)) }
    }

    override suspend fun awaitOrLoad(
        mediaId: MediaId,
        artworkType: String,
        artworkUrl: String?
    ): Flow<Resource<SongEntity?>> {
        return awaitOrLoad(
            SongEntity(
                title = "UNKNOWN",
                artist = "UNKNOWN",
                duration = 0.ms,
                mediaId = mediaId,
                artworkType = artworkType,
                artworkUrl = artworkUrl
            ),
            fetchOnline = false
        )
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

    override fun getOrNull(mediaId: MediaId) = codex[mediaId]?.artwork

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
    ): Resource<SongEntity?> {
        val res = artworkLoader.requestLoad(entity, fetchOnline)
        synchronized(codex) {
            codex[entity.mediaId]?.artwork = res.data?.artwork
            codex[entity.mediaId]?.job?.complete()
        }

        return when (res) {
            is Resource.Error -> Resource.Error(res, res.data?.entityToUpdate)
            else -> Resource.Success(res.data?.entityToUpdate)
        }
    }
}