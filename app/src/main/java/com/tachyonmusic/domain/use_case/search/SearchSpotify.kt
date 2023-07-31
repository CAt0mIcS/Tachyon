package com.tachyonmusic.domain.use_case.search

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.domain.SpotifyInterfacer

class SearchSpotify(
    private val spotifyInterfacer: SpotifyInterfacer
) {
    suspend operator fun invoke(searchText: String): List<Playback> {
        if(!spotifyInterfacer.isAuthorized)
            return emptyList()

        return spotifyInterfacer.searchTracks(searchText)
    }
}