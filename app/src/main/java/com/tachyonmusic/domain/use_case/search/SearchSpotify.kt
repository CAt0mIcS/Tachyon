package com.tachyonmusic.domain.use_case.search

import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.util.ms

class SearchSpotify {
    suspend operator fun invoke(searchText: String): List<Playback> {
        return listOf(SpotifySong(MediaId("spotify:track:320fds4392789dsfe2"), searchText, searchText, 32323.ms))
    }
}