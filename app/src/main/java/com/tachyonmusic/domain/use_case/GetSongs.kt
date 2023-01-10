package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.SongRepository

class GetSongs(
    private val repository: SongRepository
) {
    suspend operator fun invoke() =
        repository.getSongs().onEach { it.isArtworkLoading.value = true }

    // TODO: Move
    suspend fun entities() = repository.getSongEntities()
}