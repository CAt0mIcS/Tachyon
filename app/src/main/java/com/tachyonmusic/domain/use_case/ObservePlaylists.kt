package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.PlaylistRepository

class ObservePlaylists(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke() = playlistRepository.observe()
}