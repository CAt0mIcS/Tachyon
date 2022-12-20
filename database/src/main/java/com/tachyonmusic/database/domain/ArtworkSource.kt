package com.tachyonmusic.database.domain

import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.core.domain.Artwork

interface ArtworkSource {
    suspend fun get(playback: SinglePlaybackEntity): Artwork?
}