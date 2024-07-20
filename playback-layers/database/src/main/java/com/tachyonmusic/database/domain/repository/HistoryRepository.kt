package com.tachyonmusic.database.domain.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observe(): Flow<List<HistoryEntity>>

    suspend fun getHistory(): List<HistoryEntity>
    suspend operator fun plusAssign(playback: SinglePlaybackEntity)

    /**
     * Removes elements containing [mediaId]. If it's a song's media id
     * it will also remove any remixes with that song
     */
    suspend fun removeHierarchical(mediaId: MediaId)

    suspend operator fun minusAssign(playback: SinglePlaybackEntity)
    suspend operator fun minusAssign(playbacks: List<MediaId>)

    suspend fun clear()
}