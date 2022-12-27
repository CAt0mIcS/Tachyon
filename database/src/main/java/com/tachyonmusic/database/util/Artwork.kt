package com.tachyonmusic.database.util

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import java.net.URI

fun getArtworkForPlayback(playback: PlaybackEntity?, log: Logger = Log()): Artwork? =
    when (playback) {
        is SinglePlaybackEntity -> {
            when (playback.artworkType) {
                ArtworkType.NO_ARTWORK -> null
                ArtworkType.EMBEDDED -> {
                    val path = playback.mediaId.path
                    if (path == null) {
//                            updateOccurringArtwork(playback, ArtworkType.NO_ARTWORK)
                        null
                    } else {
                        val bitmap = EmbeddedArtwork.load(path)
                        if (bitmap == null) {
//                                updateOccurringArtwork(playback, ArtworkType.NO_ARTWORK)
                            null
                        } else
                            EmbeddedArtwork(bitmap)
                    }
                }

                ArtworkType.REMOTE -> {
                    if (playback.artworkUrl.isNullOrBlank()) {
//                            updateOccurringArtwork(playback, ArtworkType.NO_ARTWORK)
                        null
                    } else
                        RemoteArtwork(URI(playback.artworkUrl!!))
                }

                else -> {
                    log.error("Invalid artwork type ${playback.artworkType}")
                    null
                }
            }
        }

        else -> null
    }


data class ArtworkUpdateInfo(
    val songRepository: SongRepository,
    val loopRepository: LoopRepository,
    val playback: PlaybackEntity?,
    val artworkType: String,
    val artworkUrl: String? = null
)

/**
 * Takes a [PlaybackEntity] and updates the artwork of the underlying song wherever it's saved.
 * So if we update a loop's artwork, the artwork for the song in the [SongRepository] is also set.
 */
suspend fun updateArtwork(
    songRepository: SongRepository,
    loopRepository: LoopRepository,
    playback: PlaybackEntity?,
    artworkType: String,
    artworkUrl: String? = null
): Boolean {

    when (playback) {
        is SongEntity -> {
            songRepository.updateArtwork(playback, artworkType, artworkUrl)
            loopRepository.updateArtworkBySong(playback.mediaId, artworkType, artworkUrl)
        }

        is LoopEntity -> {
            songRepository.updateArtwork(
                songRepository.findByMediaId(
                    playback.mediaId.underlyingMediaId ?: return false
                ) ?: return false,
                artworkType,
                artworkUrl
            )
            loopRepository.updateArtworkBySong(
                playback.mediaId.underlyingMediaId ?: return false,
                artworkType,
                artworkUrl
            )
        }

        else -> return false
    }
    return true
}

suspend fun updateArtwork(info: ArtworkUpdateInfo) = updateArtwork(
    info.songRepository,
    info.loopRepository,
    info.playback,
    info.artworkType,
    info.artworkUrl
)