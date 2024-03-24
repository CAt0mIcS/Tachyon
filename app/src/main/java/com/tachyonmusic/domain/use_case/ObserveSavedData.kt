package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.DataRepository

class ObserveSavedData(
    private val dataRepository: DataRepository
) {
    operator fun invoke() = dataRepository.observe()
}