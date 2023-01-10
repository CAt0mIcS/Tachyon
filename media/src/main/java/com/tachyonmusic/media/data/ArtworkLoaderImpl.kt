package com.tachyonmusic.media.data

import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.R
import com.tachyonmusic.media.domain.ArtworkLoader
import com.tachyonmusic.media.domain.ArtworkLoader.ArtworkData
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.net.URI

internal class ArtworkLoaderImpl(
    private val artworkFetcher: ArtworkFetcher,
    private val log: Logger,
    private val metadataExtractor: SongMetadataExtractor
) : ArtworkLoader {
    override suspend fun requestLoad(entity: SinglePlaybackEntity): Resource<ArtworkData> {
        when (entity.artworkType) {
            ArtworkType.NO_ARTWORK -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.NO_ARTWORK}")
                return Resource.Success(ArtworkData())
            }

            ArtworkType.REMOTE -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.REMOTE}")
                if (entity.artworkUrl.isNullOrBlank()) {
                    entity.artworkType = ArtworkType.UNKNOWN
                    entity.artworkUrl = null
                    return Resource.Error(
                        message = UiText.StringResource(
                            R.string.no_artwork_found,
                            "${entity.title} - ${entity.artist}"
                        ),
                        data = ArtworkData(entityToUpdate = entity)
                    )
                }

                return Resource.Success(ArtworkData(RemoteArtwork(URI(entity.artworkUrl!!))))
            }

            ArtworkType.EMBEDDED -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has ${ArtworkType.EMBEDDED}")
                val path = entity.mediaId.path
                if (path != null) {
                    val embedded = EmbeddedArtwork.load(path, metadataExtractor)
                    return if (embedded != null) {
                        Resource.Success(ArtworkData(EmbeddedArtwork(embedded, path)))
                    } else {
                        Resource.Error(
                            UiText.StringResource(
                                R.string.no_embedded_artwork_despite_embedded_artwork_type,
                                path.absolutePath
                            )
                        )
                    }
                }
                entity.artworkType = ArtworkType.UNKNOWN
                return Resource.Error(
                    message = UiText.StringResource(R.string.unknown_error),
                    data = ArtworkData(entityToUpdate = entity)
                )
            }

            else -> {
                log.debug("Entity ${entity.title} - ${entity.artist} has no artwork type, trying to find artwork...")
                val newArtwork = tryFindArtwork(entity)
                val ret: Resource<ArtworkData> =
                    when (val artwork = newArtwork.data) {
                        is RemoteArtwork -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.REMOTE}")
                            entity.artworkType = ArtworkType.REMOTE
                            entity.artworkUrl = artwork.uri.toURL().toString()
                            Resource.Success(ArtworkData(artwork, entity))
                        }

                        is EmbeddedArtwork -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.EMBEDDED}")
                            entity.artworkType = ArtworkType.EMBEDDED
                            entity.artworkUrl = null
                            Resource.Success(ArtworkData(artwork, entity))
                        }

                        else -> {
                            log.debug("Entity ${entity.title} - ${entity.artist} found ${ArtworkType.NO_ARTWORK}")
                            entity.artworkType = ArtworkType.NO_ARTWORK
                            Resource.Error(
                                message = UiText.StringResource(
                                    R.string.no_artwork_found,
                                    "${entity.title} - ${entity.artist}"
                                ),
                                data = ArtworkData(entityToUpdate = entity)
                            )
                        }
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

        val bitmap = EmbeddedArtwork.load(path, metadataExtractor) ?: return Resource.Error(
            UiText.StringResource(
                R.string.invalid_path,
                path.absolutePath
            )
        )

        return Resource.Success(EmbeddedArtwork(bitmap, path))
    }
}