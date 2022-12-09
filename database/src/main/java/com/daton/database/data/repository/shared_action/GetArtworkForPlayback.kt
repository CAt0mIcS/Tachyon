package com.daton.database.data.repository.shared_action

import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import java.net.URI

class GetArtworkForPlayback {
    operator fun invoke(playback: PlaybackEntity?): Artwork? =
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
}