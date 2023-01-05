package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository

class ObserveSongs(
    private val repository: SongRepository
) {
    operator fun invoke() = repository.observe()
}