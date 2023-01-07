package com.tachyonmusic.media.data

import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.R
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.net.URI

class ArtworkCodexImpl(
    private val artworkFetcher: ArtworkFetcher,
    private val log: Logger
) : ArtworkCodex {
    private data class ArtworkData(
        var artwork: Artwork?,
        val job: CompletableJob = Job()
    )

    private val codex = mutableMapOf<MediaId, ArtworkData>()

    override suspend fun awaitOrLoad(entity: SinglePlaybackEntity): Resource<SinglePlaybackEntity?> {
        val data = synchronized(codex) {
            codex[entity.mediaId]
        }

        if (internalAwait(data?.job, entity.mediaId)) {
            /**
             * Doesn't require [SinglePlaybackEntity] database update since the other thread that
             * started the loading job will update it
             */
            return Resource.Success(data = null)
        } else if (data == null) {
            log.debug("Requesting artwork for ${entity.title} - ${entity.artist}")
            return requestLoad(entity)
        }

        log.debug("Artwork already loaded for ${entity.title} - ${entity.artist}")
        return Resource.Success(data = null)
    }

    override suspend fun awaitOrLoad(
        mediaId: MediaId,
        artworkType: String,
        artworkUrl: String?
    ): Resource<SinglePlaybackEntity?> {
        return awaitOrLoad(
            SongEntity(
                title = "UNKNOWN",
                artist = "UNKNOWN",
                duration = 0L,
                mediaId = mediaId,
                artworkType = artworkType,
                artworkUrl = artworkUrl
            )
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

    private suspend fun requestLoad(entity: SinglePlaybackEntity): Resource<SinglePlaybackEntity?> {
        synchronized(codex) {
            codex[entity.mediaId] = ArtworkData(artwork = null)
        }

        when (entity.artworkType) {
            ArtworkType.NO_ARTWORK -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.NO_ARTWORK}")
                synchronized(codex) {
                    codex[entity.mediaId]?.job?.complete()
                }
                return Resource.Success(data = null)
            }

            ArtworkType.REMOTE -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.REMOTE}")
                synchronized(codex) {
                    codex[entity.mediaId]?.artwork = RemoteArtwork(URI(entity.artworkUrl!!))
                    codex[entity.mediaId]?.job?.complete()
                }
                return Resource.Success(data = null)
            }

            ArtworkType.EMBEDDED -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.EMBEDDED}")
                val path = entity.mediaId.path
                if (path != null) {
                    val embedded = EmbeddedArtwork.load(path)
                    return if (embedded != null) {
                        synchronized(codex) {
                            codex[entity.mediaId]?.artwork = EmbeddedArtwork(embedded, path)
                            codex[entity.mediaId]?.job?.complete()
                        }
                        Resource.Success(data = null)
                    } else {
                        synchronized(codex) {
                            codex[entity.mediaId]?.job?.complete()
                        }

                        Resource.Error(
                            UiText.StringResource(
                                R.string.no_embedded_artwork_despite_embedded_artwork_type,
                                path.absolutePath
                            )
                        )
                    }
                }
                synchronized(codex) {
                    codex[entity.mediaId]?.job?.complete()
                }

                entity.artworkType = ArtworkType.UNKNOWN
                return Resource.Error(
                    message = UiText.StringResource(R.string.unknown_error),
                    data = entity
                )
            }

            else -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has no artwork type, trying to find artwork...")
                val newArtwork = tryFindArtwork(entity)
                val ret: Resource<SinglePlaybackEntity?> =
                    when (val artworkData = newArtwork.data) {
                        is RemoteArtwork -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.REMOTE}")
                            entity.artworkType = ArtworkType.REMOTE
                            entity.artworkUrl = artworkData.uri.toURL().toString()
                            Resource.Success(entity)
                        }

                        is EmbeddedArtwork -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.EMBEDDED}")
                            entity.artworkType = ArtworkType.EMBEDDED
                            entity.artworkUrl = null
                            Resource.Success(entity)
                        }

                        else -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.NO_ARTWORK}")
                            entity.artworkType = ArtworkType.NO_ARTWORK
                            Resource.Error(
                                message = UiText.StringResource(
                                    R.string.no_artwork_found,
                                    "${entity.title} - ${entity.artist}"
                                ),
                                data = entity
                            )
                        }
                    }

                synchronized(codex) {
                    codex[entity.mediaId]?.artwork = newArtwork.data
                    codex[entity.mediaId]?.job?.complete()
                }

                return ret
            }
        }
    }

    private suspend fun tryFindArtwork(
        entity: SinglePlaybackEntity
    ): Resource<Artwork?> {
        var res = tryFindEmbeddedArtwork(entity)
        if (res is Resource.Success) // TODO: Return error message from embedded artwork loading too
            return res

        res = tryFindRemoteArtwork(entity)
        return res
    }


    private suspend fun tryFindRemoteArtwork(
        entity: SinglePlaybackEntity,
    ): Resource<Artwork?> {
        var ret: Resource<Artwork?> =
            Resource.Error(UiText.StringResource(R.string.unknown_error))

        artworkFetcher.query(entity.title, entity.artist, 1000)
            .onEach { res ->
                if (res is Resource.Success) {
                    ret = Resource.Success(RemoteArtwork(URI(res.data!!)))
                } else if (res is Resource.Error) {
                    ret = Resource.Error(
                        message = res.message ?: UiText.StringResource(R.string.unknown_error),
                        exception = res.exception
                    )
                }
            }.collect()

        return ret
    }

    private fun tryFindEmbeddedArtwork(entity: SinglePlaybackEntity): Resource<Artwork?> {
        val path = entity.mediaId.path
            ?: return Resource.Error(UiText.StringResource(R.string.invalid_path, "null"))

        val bitmap = EmbeddedArtwork.load(path) ?: return Resource.Error(
            UiText.StringResource(
                R.string.invalid_path,
                path.absolutePath
            )
        )

        return Resource.Success(EmbeddedArtwork(bitmap, path))
    }
}