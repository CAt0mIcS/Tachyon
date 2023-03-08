package com.tachyonmusic.database.data.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.data.data_source.HistoryDao
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository

class RoomHistoryRepository(
    private val dao: HistoryDao
) : HistoryRepository {

    override fun observe() = dao.observe()

    override suspend fun getHistory() = dao.getHistory()

    override suspend fun plusAssign(playback: SinglePlaybackEntity) {
        dao.addHistory(HistoryEntity(playback.mediaId))
    }

    override suspend fun removeHierarchical(mediaId: MediaId) {
        dao.removeHistoryHierarchical(mediaId)
    }

    override suspend fun minusAssign(playback: SinglePlaybackEntity) {
        dao.removeHistory(playback.mediaId)
    }

    override suspend fun minusAssign(playbacks: List<MediaId>) {
        dao.removeHistory(playbacks)
    }

    override suspend fun clear() {
        dao.clear()
    }
}