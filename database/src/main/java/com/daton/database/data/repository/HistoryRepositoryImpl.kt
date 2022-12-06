package com.daton.database.data.repository

import com.daton.database.data.data_source.HistoryDao
import com.daton.database.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val dao: HistoryDao
) : HistoryRepository {

}