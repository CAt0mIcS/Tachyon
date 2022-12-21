package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.PlaylistRepository

class GetPlaylists(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke() = playlistRepository.getPlaylists()
}