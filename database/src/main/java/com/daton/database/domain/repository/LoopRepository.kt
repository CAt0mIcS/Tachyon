package com.daton.database.domain.repository

import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.MediaId

interface LoopRepository {
    suspend fun findBySong(song: SongEntity): LoopEntity?
    suspend fun findByMediaId(mediaId: MediaId): LoopEntity?
    suspend fun updateArtwork(loop: LoopEntity, artworkType: String, artworkUrl: String? = null)
}