package com.tachyonmusic.domain.use_case.main

import android.util.Log
import com.daton.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateArtworks(
    private val songRepo: SongRepository,
    private val isFirstAppStart: IsFirstAppStart
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        if (isFirstAppStart()) {
            Log.d("UpdateSongDatabase", "Loading song artworks")
            songRepo.loadArtworks()
        }
    }
}