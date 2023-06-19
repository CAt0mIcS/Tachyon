package com.tachyonmusic.domain.use_case.search

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.SpotifyInterfacer

class SearchSpotify(
    private val spotifyInterfacer: SpotifyInterfacer
) {
    suspend operator fun invoke(searchText: String): List<Playback> {
        return spotifyInterfacer.searchTracks(searchText)
    }
}