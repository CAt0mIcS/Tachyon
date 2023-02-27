package com.tachyonmusic.database.data.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.database.data.data_source.DataDao
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import kotlinx.coroutines.flow.Flow

class RoomDataRepository(
    private val dao: DataDao
) : DataRepository {
    override suspend fun getData(): DataEntity = dao.getData() ?: setData(DataEntity())

    override suspend fun setData(data: DataEntity): DataEntity {
        dao.setData(data)
        return data
    }

    override fun observe(): Flow<DataEntity> = dao.observe()

    override suspend fun setRecentlyPlayed(recentlyPlayed: RecentlyPlayed) {
        dao.setRecentlyPlayed(
            recentlyPlayed.mediaId,
            recentlyPlayed.position,
            recentlyPlayed.duration,
            recentlyPlayed.artworkType,
            recentlyPlayed.artworkUrl
        )
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) {
        dao.setRepeatMode(repeatMode)
    }
}