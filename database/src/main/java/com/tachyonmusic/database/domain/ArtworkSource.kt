package com.tachyonmusic.database.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity

interface ArtworkSource {
    suspend fun get(playback: SinglePlaybackEntity): Artwork?
}