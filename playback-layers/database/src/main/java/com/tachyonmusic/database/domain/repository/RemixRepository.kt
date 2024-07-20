package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.Flow

interface RemixRepository {
    fun observe(): Flow<List<RemixEntity>>

    suspend fun getRemixes(): List<RemixEntity>
    suspend fun add(remix: RemixEntity): Resource<Unit>
    suspend fun addAll(remixes: List<RemixEntity>): Resource<Unit>

    suspend fun remove(mediaId: MediaId)
    suspend fun removeIf(pred: (RemixEntity) -> Boolean)

    suspend fun findBySong(songTitle: String, songArtist: String, songDuration: Duration): RemixEntity?
    suspend fun findByMediaId(mediaId: MediaId): RemixEntity?

    suspend fun updateMetadata(song: MediaId, newMediaId: MediaId)
}