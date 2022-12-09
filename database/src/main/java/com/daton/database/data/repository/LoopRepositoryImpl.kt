package com.daton.database.data.repository

import com.daton.database.data.data_source.LoopDao
import com.daton.database.data.repository.shared_action.UpdateArtwork
import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.LoopRepository
import com.tachyonmusic.core.domain.MediaId

class LoopRepositoryImpl(
    private val dao: LoopDao
) : LoopRepository {

    override suspend fun findBySong(song: SongEntity): LoopEntity? {
        return null
    }

    override suspend fun findByMediaId(mediaId: MediaId): LoopEntity? {
        return null
    }

    override suspend fun updateArtwork(loop: LoopEntity, artworkType: String, artworkUrl: String?) {

    }
}