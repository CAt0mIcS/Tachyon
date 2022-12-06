package com.tachyonmusic.domain.use_case

import com.daton.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke() =
        withContext(Dispatchers.IO) { return@withContext repository.getAll() }
}