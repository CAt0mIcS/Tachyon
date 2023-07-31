package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observe(): Flow<List<HistoryEntity>>

    suspend fun getHistory(): List<HistoryEntity>
    suspend operator fun plusAssign(mediaId: MediaId)

    /**
     * Removes elements containing [mediaId]. If it's a song's media id
     * it will also remove any customizedSongs with that song
     */
    suspend fun removeHierarchical(mediaId: MediaId)

    suspend operator fun minusAssign(mediaId: MediaId)
    suspend operator fun minusAssign(mediaIds: List<MediaId>)

    suspend fun clear()
}