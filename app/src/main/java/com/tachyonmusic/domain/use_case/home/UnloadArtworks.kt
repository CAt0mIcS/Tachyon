package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.use_case.library.AssignArtworkToPlayback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnloadArtworks(
    private val repository: SongRepository,
    private val assignArtworkToPlayback: AssignArtworkToPlayback
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        repository.getSongsWithArtworkTypes(ArtworkType.EMBEDDED, ArtworkType.REMOTE)
            .forEach { song ->
                assignArtworkToPlayback(song.mediaId, ArtworkType.UNKNOWN)
            }
    }
}