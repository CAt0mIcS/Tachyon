package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.R
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class LoadPlaylistForPlayback(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(playback: Playback?): Resource<Pair<List<MediaItem>, Int>> {
        var initialWindowIndex: Int? = null
        var items: List<MediaItem>? = null

        if (settingsRepository.getSettings().combineDifferentPlaybackTypes) {
            when (playback) {
                is Song -> {
                    val songs = songRepository.getSongs()
                    val loops = loopRepository.getLoops()
                    initialWindowIndex = songs.indexOf(playback)
                    items =
                        songs.map { it.toMediaItem() } + loops.map { it.toMediaItem() }
                }

                is Loop -> {
                    val songs = songRepository.getSongs()
                    val loops = loopRepository.getLoops()
                    initialWindowIndex = loops.indexOf(playback)
                    items =
                        loops.map { it.toMediaItem() } + songs.map { it.toMediaItem() }
                }

                is Playlist -> {
                    initialWindowIndex = playback.currentPlaylistIndex
                    items = playback.toMediaItemList()
                }

                null -> {
                    return Resource.Error(UiText.StringResource(R.string.invalid_playback))
                }
            }
        } else {
            when (playback) {
                is Song -> {
                    val songs = songRepository.getSongs()
                    initialWindowIndex = songs.indexOf(playback)
                    items = songs.map { it.toMediaItem() }
                }

                is Loop -> {
                    val loops = loopRepository.getLoops()
                    initialWindowIndex = loops.indexOf(playback)
                    items = loops.map { it.toMediaItem() }
                }

                is Playlist -> {
                    items = playback.toMediaItemList()
                    initialWindowIndex = playback.currentPlaylistIndex
                }

                null -> {
                    return Resource.Error(UiText.StringResource(R.string.invalid_playback))
                }
            }
        }

        if (items == null || initialWindowIndex == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        return Resource.Success(items to initialWindowIndex)
    }
}