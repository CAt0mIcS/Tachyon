package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateAndSaveNewPlaylist(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String?) = withContext(Dispatchers.IO) {
        if (name.isNullOrBlank())
            return@withContext Resource.Error(
                UiText.StringResource(
                    R.string.invalid_name,
                    name.toString()
                )
            )

        if (playlistRepository.hasPlaylist(MediaId.ofLocalPlaylist(name)))
            return@withContext Resource.Error(
                UiText.StringResource(
                    R.string.invalid_name,
                    name.toString()
                )
            )

        playlistRepository.add(
            PlaylistEntity(
                MediaId.ofLocalPlaylist(name),
                emptyList()
            )
        )
    }
}