package com.tachyonmusic.domain.use_case.main

import com.daton.database.domain.ArtworkType
import com.daton.database.domain.repository.SongRepository
import com.daton.database.util.toSong

class UnloadArtworks(
    private val repository: SongRepository
) {
    suspend operator fun invoke() {
        repository.getSongsWithArtworkTypes(ArtworkType.EMBEDDED, ArtworkType.REMOTE)
            .forEach { song ->
                repository.updateArtwork(song, ArtworkType.NO_ARTWORK)
                song.toSong().artwork.value = null
            }
    }
}