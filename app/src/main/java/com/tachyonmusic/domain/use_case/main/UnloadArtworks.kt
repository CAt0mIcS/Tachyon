package com.tachyonmusic.domain.use_case.main

import com.daton.database.data.repository.shared_action.ConvertEntityToSong
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.repository.SongRepository

class UnloadArtworks(
    private val repository: SongRepository,
    private val convertEntityToSong: ConvertEntityToSong
) {
    suspend operator fun invoke() {
        repository.getSongsWithArtworkTypes(ArtworkType.EMBEDDED, ArtworkType.REMOTE)
            .forEach { song ->
                repository.updateArtwork(song, ArtworkType.NO_ARTWORK)
                convertEntityToSong(song).artwork.value = null
            }
    }
}