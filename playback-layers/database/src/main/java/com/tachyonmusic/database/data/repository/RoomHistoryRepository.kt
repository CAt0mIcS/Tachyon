package com.tachyonmusic.database.data.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.data.data_source.HistoryDao
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository

class RoomHistoryRepository(
    private val dao: HistoryDao
) : HistoryRepository {

    override fun observe() = dao.observe()

    override suspend fun getHistory() = dao.getHistory()

    override suspend fun plusAssign(mediaId: MediaId) {
        dao.addHistory(HistoryEntity(mediaId))
    }

    override suspend fun removeHierarchical(mediaId: MediaId) {
        dao.removeHistoryHierarchical(mediaId)
    }

    override suspend fun minusAssign(mediaId: MediaId) {
        dao.removeHistory(mediaId)
    }

    override suspend fun minusAssign(mediaIds: List<MediaId>) {
        dao.removeHistory(mediaIds)
    }

    override suspend fun clear() {
        dao.clear()
    }
}