package com.tachyonmusic.artwork.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import com.tachyonmusic.util.Resource

interface ArtworkLoader {
    data class ArtworkData(
        val artwork: Artwork? = null,
        val entityToUpdate: SongPermissionEntity? = null
    )

    suspend fun requestLoad(
        entity: SongPermissionEntity,
        fetchOnline: Boolean = true
    ): Resource<ArtworkData>
}