package com.daton.database.domain

import com.daton.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.core.domain.Artwork

interface ArtworkSource {
    suspend fun get(playback: SinglePlaybackEntity): Artwork?
}