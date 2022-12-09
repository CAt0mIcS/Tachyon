package com.tachyonmusic.domain.use_case

import com.daton.database.domain.repository.SongRepository

class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke() = repository.getSongs()
}