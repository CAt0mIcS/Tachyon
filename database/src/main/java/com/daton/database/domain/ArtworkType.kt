package com.daton.database.domain

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.SinglePlayback

object ArtworkType {
    const val NO_ARTWORK = "NONE"
    const val EMBEDDED = "EMBEDDED"
    const val REMOTE = "REMOTE"

    fun getType(playback: SinglePlayback) = when (playback.artwork.value) {
        is RemoteArtwork -> REMOTE
        is EmbeddedArtwork -> EMBEDDED
        else -> NO_ARTWORK // TODO: Add warning of unknown artwork type
    }
}