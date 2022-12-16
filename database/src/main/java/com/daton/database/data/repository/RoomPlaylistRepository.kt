package com.daton.database.data.repository

import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.domain.model.PlaylistEntity
import com.daton.database.domain.repository.PlaylistRepository
import com.tachyonmusic.core.domain.MediaId

class RoomPlaylistRepository(
    private val dao: PlaylistDao
) : PlaylistRepository {
    override suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity? {
        return null
    }
}