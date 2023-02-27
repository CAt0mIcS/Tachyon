package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.repository.DataRepository

class GetSavedData(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke() = dataRepository.getData()
}