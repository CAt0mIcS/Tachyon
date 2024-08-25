package com.tachyonmusic.playback_layers.data

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus

class PredefinedPlaylistsRepositoryImpl(
    playbackRepository: PlaybackRepository,
    settingsRepository: SettingsRepository,
    externalScope: CoroutineScope
) : PredefinedPlaylistsRepository {
    private val _songPlaylist = MutableStateFlow<List<Playback>>(emptyList())
    override val songPlaylist = _songPlaylist.asStateFlow()

    private val _remixPlaylist = MutableStateFlow<List<Playback>>(emptyList())
    override val remixPlaylist = _remixPlaylist.asStateFlow()

    init {
        combine(
            playbackRepository.songFlow,
            playbackRepository.remixFlow,
            settingsRepository.observe()
        ) { songs, remixes, settings ->
            val filteredSongs = songs.filter { it.isPlayable && !it.isHidden }
            val filteredRemixes = remixes.filter { it.isPlayable }

            if (settings.combineDifferentPlaybackTypes) {
                _songPlaylist.update { filteredSongs + filteredRemixes }
                _remixPlaylist.update { filteredRemixes + filteredSongs }
            } else {
                _songPlaylist.update { filteredSongs }
                _remixPlaylist.update { filteredRemixes }
            }
        }.launchIn(externalScope + Dispatchers.IO)
    }
}