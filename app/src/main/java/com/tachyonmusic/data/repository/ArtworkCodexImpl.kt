package com.tachyonmusic.data.repository

import com.tachyonmusic.app.R
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.domain.repository.ArtworkCodex
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.net.URI
import java.security.InvalidParameterException

class ArtworkCodexImpl(
    private val artworkFetcher: ArtworkFetcher,
    private val log: Logger
) : ArtworkCodex {
    private val codex = mutableMapOf<MediaId, Artwork?>()

    override suspend fun requestLoad(song: SongEntity): Resource<SongEntity?> {
        when (song.artworkType) {
            ArtworkType.NO_ARTWORK -> {
                synchronized(codex) {
                    codex[song.mediaId] = null
                }
                return Resource.Success(data = null)
            }

            ArtworkType.REMOTE -> {
                synchronized(codex) {
                    codex[song.mediaId] = RemoteArtwork(URI(song.artworkUrl!!))
                }
                return Resource.Success(data = null)
            }

            ArtworkType.EMBEDDED -> {
                val path = song.mediaId.path
                if (path != null) {
                    val embedded = EmbeddedArtwork.load(path)
                    return if (embedded != null) {
                        synchronized(codex) {
                            codex[song.mediaId] = EmbeddedArtwork(embedded, path)
                        }
                        Resource.Success(data = null)
                    } else {
                        Resource.Error(
                            UiText.StringResource(
                                R.string.no_embedded_artwork_despite_embedded_artwork_type,
                                path.absolutePath
                            )
                        )
                    }
                }
                song.artworkType = ArtworkType.UNKNOWN
                return Resource.Error(
                    message = UiText.StringResource(R.string.unknown_error),
                    data = song
                )
            }

            else -> {
                val newArtwork = tryFindArtwork(song)
                return if (newArtwork.data != null) {
                    song.artworkType = ArtworkType.REMOTE
                    song.artworkUrl = newArtwork.data!!.uri.toURL().toString()
                    synchronized(codex) {
                        codex[song.mediaId] = newArtwork.data!!
                    }
                    Resource.Success(song)
                } else {
                    song.artworkType = ArtworkType.NO_ARTWORK
                    synchronized(codex) {
                        codex[song.mediaId] = null
                    }
                    Resource.Error(
                        message = UiText.StringResource(
                            R.string.no_artwork_found,
                            "${song.title} - ${song.artist}"
                        ),
                        data = song
                    )
                }
            }
        }
    }

    override fun get(mediaId: MediaId): Artwork? {
        if (codex.containsKey(mediaId))
            return codex[mediaId]
        throw NoSuchElementException("Key $mediaId is missing in the map.")
    }

    override fun isLoaded(mediaId: MediaId) = codex.containsKey(mediaId)


    private suspend fun tryFindArtwork(
        song: SongEntity,
    ): Resource<RemoteArtwork> {
        var ret: Resource<RemoteArtwork> =
            Resource.Error(UiText.StringResource(R.string.unknown_error))

        artworkFetcher.query(song.title, song.artist, 1000)
            .onEach { res ->
                if (res is Resource.Success) {
                    log.info("Successfully loaded artwork for ${song.title} - ${song.artist}")
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
}