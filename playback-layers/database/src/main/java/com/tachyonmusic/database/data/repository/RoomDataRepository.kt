package com.tachyonmusic.database.data.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.database.data.data_source.DataDao
import com.tachyonmusic.database.domain.model.DataEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataRepository(
    private val dao: DataDao
) : DataRepository {
    override suspend fun getData(): DataEntity = dao.getData() ?: setData(DataEntity())

    override suspend fun setData(data: DataEntity): DataEntity {
        dao.setData(data)
        return data
    }

    override fun observe(): Flow<DataEntity> = dao.observe().map {
        it ?: DataEntity()
    }

    override suspend fun update(
        recentlyPlayed: RecentlyPlayed?,
        maxRemixCount: Int?,
        repeatMode: RepeatMode?,
        onboardingCompleted: Boolean?
    ) {
        if (recentlyPlayed != null)
            dao.setRecentlyPlayed(
                recentlyPlayed.mediaId,
                recentlyPlayed.position,
                recentlyPlayed.duration,
                recentlyPlayed.artworkType,
                recentlyPlayed.artworkUrl
            )

        if(maxRemixCount != null)
            dao.setMaxRemixCount(maxRemixCount)

        if (repeatMode != null)
            dao.setRepeatMode(repeatMode)

//        if(onboardingCompleted != null)
//            dao.setOnboarding(onboardingCompleted)
    }
}