package com.daton.database.data.repository

import com.daton.database.data.data_source.DataDao
import com.daton.database.domain.model.DataEntity
import com.daton.database.domain.repository.DataRepository
import com.daton.database.domain.repository.RecentlyPlayed

class DataRepositoryImpl(
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