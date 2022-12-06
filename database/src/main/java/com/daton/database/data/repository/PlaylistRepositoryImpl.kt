package com.daton.database.data.repository

import com.daton.database.data.data_source.PlaylistDao
import com.daton.database.domain.repository.PlaylistRepository

class PlaylistRepositoryImpl(
    private val dao: PlaylistDao
) : PlaylistRepository {
}