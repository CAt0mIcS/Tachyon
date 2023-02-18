package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class CreateAndSaveNewPlaylist(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String?): Resource<Unit> {
        if (name.isNullOrBlank())
            return Resource.Error(UiText.StringResource(R.string.invalid_name, name.toString()))

        // TODO: Optimize
        if (playlistRepository.getPlaylists().any { it.name == name })
            return Resource.Error(UiText.StringResource(R.string.invalid_name, name.toString()))

        return playlistRepository.add(
            PlaylistEntity(
                MediaId.ofRemotePlaylist(name),
                emptyList()
            )
        )
    }
}