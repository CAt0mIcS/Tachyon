package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.repository.PlaylistRepository

class CreateAndSaveNewPlaylist(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String?) {
        if (name.isNullOrBlank())
            return

        // TODO: Optimize
        if (playlistRepository.getPlaylists().any { it.name == name })
            return

        playlistRepository.add(
            PlaylistEntity(
                MediaId.ofRemotePlaylist(name),
                emptyList()
            )
        )
    }
}