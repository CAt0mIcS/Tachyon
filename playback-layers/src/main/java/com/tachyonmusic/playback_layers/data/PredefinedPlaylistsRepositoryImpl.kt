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
            val filteredSongs = songs.filter { it.isPlayable && !it.isHidden }
            val filteredCustomizedSongs = customizedSongs.filter { it.isPlayable }

            if (settings.combineDifferentPlaybackTypes) {
                _songPlaylist.update { filteredSongs + filteredCustomizedSongs }
                _customizedSongPlaylist.update { filteredCustomizedSongs + filteredSongs }
            } else {
                _songPlaylist.update { filteredSongs }
                _customizedSongPlaylist.update { filteredCustomizedSongs }
            }
        }.launchIn(externalScope + Dispatchers.IO)
    }
}