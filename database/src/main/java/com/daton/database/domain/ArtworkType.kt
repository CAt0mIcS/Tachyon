package com.daton.database.domain

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger

object ArtworkType {
    const val NO_ARTWORK = "NONE"
    const val EMBEDDED = "EMBEDDED"
    const val REMOTE = "REMOTE"

    fun getType(playback: SinglePlayback, log: Logger = Log()) = when (playback.artwork.value) {
        is RemoteArtwork -> REMOTE
        is EmbeddedArtwork -> EMBEDDED
        null -> NO_ARTWORK
        else -> {
            log.warning("Unknown artwork type ${playback.artwork.value!!::class.java.name}")
            NO_ARTWORK
        }
    }
}