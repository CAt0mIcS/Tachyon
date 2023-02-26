package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetHistory(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) { historyRepository.getHistory() }
}