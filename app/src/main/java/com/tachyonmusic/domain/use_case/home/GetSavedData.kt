package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.database.domain.repository.DataRepository

class GetSavedData(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke() = dataRepository.getData()
}