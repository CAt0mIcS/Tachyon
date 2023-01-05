package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.repository.HistoryRepository

class ObserveHistory(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke() = historyRepository.observe()
}