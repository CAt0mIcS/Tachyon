package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.media.R
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadPlaylistForPlayback(
    private val repository: UserRepository
) {
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        var initialWindowIndex: Int? = null
        var items: List<MediaItem>? = null

        when (playback) {
            is Song -> {
                initialWindowIndex = repository.songs.value.indexOf(playback)
                items = repository.songs.value.map { it.toMediaItem() }
            }
            is Loop -> {
                initialWindowIndex = repository.loops.value.indexOf(playback)
                items = repository.loops.value.map { it.toMediaItem() }
            }
            is Playlist -> {
                items = playback.toMediaItemList()
                initialWindowIndex = playback.currentPlaylistIndex
            }
            null -> {
                return@withContext Resource.Error(UiText.StringResource(R.string.invalid_playback))
            }
        }

        Resource.Success(items to initialWindowIndex)
    }
}