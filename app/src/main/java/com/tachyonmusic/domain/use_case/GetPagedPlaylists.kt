package com.tachyonmusic.domain.use_case

import com.tachyonmusic.database.domain.repository.PlaylistRepository

class GetPagedPlaylists(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(pageSize: Int) = playlistRepository.getPagedPlaylists(pageSize)
}
