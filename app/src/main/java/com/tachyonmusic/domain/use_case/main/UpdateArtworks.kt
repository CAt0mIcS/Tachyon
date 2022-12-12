package com.tachyonmusic.domain.use_case.main

import android.util.Log
import com.daton.database.data.repository.shared_action.LoadArtwork
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateArtworks(
    private val songRepo: SongRepository,
    private val isFirstAppStart: IsFirstAppStart,
    private val loadArtwork: LoadArtwork
) {
    suspend operator fun invoke(ignoreIsFirstAppStart: Boolean = false) = withContext(Dispatchers.IO) {
        if (ignoreIsFirstAppStart || isFirstAppStart()) {
            Log.d("UpdateSongDatabase", "Loading song artworks")
            songRepo.getSongsWithArtworkTypes(ArtworkType.NO_ARTWORK).forEach { song ->
                loadArtwork(song)
            }
        }
    }
}