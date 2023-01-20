package com.tachyonmusic.media.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.util.Resource

interface ArtworkLoader {
    data class ArtworkData(
        val artwork: Artwork? = null,
        val entityToUpdate: SinglePlaybackEntity? = null
    )

    suspend fun requestLoad(entity: SinglePlaybackEntity, fetchOnline: Boolean = true): Resource<ArtworkData>
}