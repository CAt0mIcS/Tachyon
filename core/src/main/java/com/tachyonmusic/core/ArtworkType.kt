package com.tachyonmusic.core

import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback

object ArtworkType {
    const val NO_ARTWORK = "NONE"
    const val EMBEDDED = "EMBEDDED"
    const val REMOTE = "REMOTE"
    const val UNKNOWN = "UNKNOWN"

    fun getType(artwork: Artwork?) = when (artwork) {
        is RemoteArtwork -> REMOTE
        is EmbeddedArtwork -> EMBEDDED
        else -> UNKNOWN
    }

    fun getType(playback: Playback) =
        getType(playback.artwork)
}