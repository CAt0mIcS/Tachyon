package com.tachyonmusic.media.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource

interface ArtworkLoader {
    data class ArtworkData(
        val artwork: Artwork? = null,
        val entityToUpdate: SongEntity? = null
    )

    suspend fun requestLoad(entity: SongEntity, fetchOnline: Boolean = true): Resource<ArtworkData>
}