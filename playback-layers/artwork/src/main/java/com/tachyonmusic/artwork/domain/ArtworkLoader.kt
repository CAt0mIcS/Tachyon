package com.tachyonmusic.artwork.domain

import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource

interface ArtworkLoader {
    suspend fun requestLoad(
        entity: SongEntity,
        fetchOnline: Boolean = true
    ): Resource<ArtworkCodex.ArtworkUpdateData>
}