package com.daton.database.data.repository

import com.daton.database.data.data_source.LoopDao
import com.daton.database.domain.repository.LoopRepository

class LoopRepositoryImpl(
    private val dao: LoopDao
) : LoopRepository {
}