package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetRecentlyPlayed(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val data = dataRepository.getData()
        RecentlyPlayed(
            data.recentlyPlayedMediaId ?: return@withContext null,
            data.currentPositionInRecentlyPlayedPlayback,
            data.recentlyPlayedDuration,
            data.recentlyPlayedArtworkType,
            data.recentlyPlayedArtworkUrl
        )
    }
}