package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveRecentlyPlayed(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(recentlyPlayed: RecentlyPlayed) = withContext(Dispatchers.IO) {
        dataRepository.updateRecentlyPlayed(recentlyPlayed)
    }
}