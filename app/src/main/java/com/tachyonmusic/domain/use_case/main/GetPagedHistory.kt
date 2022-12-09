package com.tachyonmusic.domain.use_case.main

import com.daton.database.domain.repository.HistoryRepository

class GetPagedHistory(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(pageSize: Int, prefetchDistance: Int = pageSize) =
        historyRepository.getPagedHistory(pageSize, prefetchDistance)
}