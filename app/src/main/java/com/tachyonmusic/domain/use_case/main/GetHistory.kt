package com.tachyonmusic.domain.use_case.main

import com.daton.database.domain.repository.HistoryRepository

class GetHistory(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() = historyRepository.getHistory()
}