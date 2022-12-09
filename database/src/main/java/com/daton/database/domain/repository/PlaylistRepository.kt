package com.daton.database.domain.repository

import com.daton.database.domain.model.PlaylistEntity
import com.tachyonmusic.core.domain.MediaId

interface PlaylistRepository {
    suspend fun findByMediaId(mediaId: MediaId): PlaylistEntity?
}