package com.tachyonmusic.database.data.repository

import com.tachyonmusic.database.data.data_source.DataDao
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed

class RoomDataRepository(
    private val dao: DataDao
) : DataRepository {
    override suspend fun getData(): DataEntity = dao.getData() ?: setData(DataEntity())

    override suspend fun setData(data: DataEntity): DataEntity {
        dao.setData(data)
        return data
    }

    override suspend fun updateRecentlyPlayed(recentlyPlayed: RecentlyPlayed) {
        dao.updateRecentlyPlayed(recentlyPlayed.positionMs, recentlyPlayed.durationMs)
    }
}