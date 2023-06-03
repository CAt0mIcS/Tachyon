package com.tachyonmusic.playback_layers.data

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus

class PredefinedPlaylistsRepositoryImpl(
    playbackRepository: PlaybackRepository,
    settingsRepository: SettingsRepository,
    externalScope: CoroutineScope
) : PredefinedPlaylistsRepository {
    private val _songPlaylist = MutableStateFlow<List<SinglePlayback>>(emptyList())
    override val songPlaylist = _songPlaylist.asStateFlow()

    private val _customizedSongPlaylist = MutableStateFlow<List<SinglePlayback>>(emptyList())
    override val customizedSongPlaylist = _customizedSongPlaylist.asStateFlow()

    init {
        combine(
            playbackRepository.songFlow,
            playbackRepository.customizedSongFlow,
            settingsRepository.observe()
        ) { songs, customizedSongs, settings ->
            if (settings.combineDifferentPlaybackTypes) {
                _songPlaylist.update { songs + customizedSongs }
                _customizedSongPlaylist.update { customizedSongs + songs }
            } else {
                _songPlaylist.update { songs }
                _customizedSongPlaylist.update { customizedSongs }
            }
        }.launchIn(externalScope + Dispatchers.IO)
    }
}