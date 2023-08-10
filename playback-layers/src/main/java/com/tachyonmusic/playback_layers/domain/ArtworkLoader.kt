package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface ArtworkLoader {
    suspend fun requestLoad(
        entity: SongEntity,
        fetchOnline: Boolean = true
    ): Resource<ArtworkCodex.ArtworkUpdateData>

    fun findAllArtwork(
        mediaId: MediaId,
        query: String,
        pageSize: Int
    ): Flow<Resource<Artwork>>
}