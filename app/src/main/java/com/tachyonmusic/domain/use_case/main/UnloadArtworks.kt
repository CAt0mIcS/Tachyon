package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnloadArtworks(
    private val repository: SongRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        repository.getSongsWithArtworkTypes(ArtworkType.EMBEDDED, ArtworkType.REMOTE)
            .forEach { song ->
                repository.updateArtwork(song, ArtworkType.UNKNOWN)
            }
    }
}