package com.daton.database.util

import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.SinglePlaybackEntity
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import java.net.URI

fun getArtworkForPlayback(playback: PlaybackEntity?): Artwork? =
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

                else -> TODO("Invalid artwork type ${playback.artworkType}")
            }
        }

        else -> null
    }


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
) {

    when (playback) {
        is SongEntity -> {
            songRepository.updateArtwork(playback, artworkType, artworkUrl)
            loopRepository.updateArtworkBySong(playback.mediaId, artworkType, artworkUrl)
        }

        is LoopEntity -> {
            songRepository.updateArtwork(
                songRepository.findByMediaId(
                    playback.mediaId.underlyingMediaId ?: return
                ) ?: return,
                artworkType,
                artworkUrl
            )
            loopRepository.updateArtwork(playback, artworkType, artworkUrl)
        }

        else -> {}
    }

}