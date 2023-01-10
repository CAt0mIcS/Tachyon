package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.repository.SongRepository

class UnloadArtworks(
    private val repository: SongRepository
) {
    suspend operator fun invoke() {
        repository.getSongsWithArtworkTypes(ArtworkType.EMBEDDED, ArtworkType.REMOTE)
            .forEach { song ->
                repository.updateArtwork(song, ArtworkType.UNKNOWN)
            }
    }
}