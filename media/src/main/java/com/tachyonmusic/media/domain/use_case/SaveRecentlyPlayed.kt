package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed

class SaveRecentlyPlayed(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(recentlyPlayed: RecentlyPlayed) =
        dataRepository.updateRecentlyPlayed(recentlyPlayed)
}