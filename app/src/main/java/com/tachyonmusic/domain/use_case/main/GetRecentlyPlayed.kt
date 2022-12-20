package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed

class GetRecentlyPlayed(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(): RecentlyPlayed {
        val data = dataRepository.getData()
        return RecentlyPlayed(
            data.currentPositionInRecentlyPlayedPlaybackMs,
            data.recentlyPlayedDurationMs
        )
    }
}