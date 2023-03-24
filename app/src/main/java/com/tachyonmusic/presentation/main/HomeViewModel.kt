package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.main.GetSavedData
import com.tachyonmusic.domain.use_case.main.UnloadArtworks
import com.tachyonmusic.domain.use_case.main.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    playbackRepository: PlaybackRepository,

    setRepeatMode: SetRepeatMode,
    getSavedData: GetSavedData,
    observeSettings: ObserveSettings,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,

    private val playPlayback: PlayPlayback,

    private val unloadArtworks: UnloadArtworks
) : ViewModel() {

    val history = playbackRepository.historyFlow.map { history ->
        // TODO: Optimize? How long does this take in total for all playback states
        history.map {
            it.copy()
        }
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )


    init {
        viewModelScope.launch {
            // Make sure browser repeat mode is up to date with saved one
            setRepeatMode(withContext(Dispatchers.IO) { getSavedData().repeatMode })
        }

        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()

            observeSettings().onEach {
                if (it.musicDirectories.isNotEmpty())
                    updateSongDatabase(it)
            }.collect()
        }
    }

    fun onItemClicked(playback: Playback) {
        viewModelScope.launch {
            playPlayback(playback, playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST)
        }
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
        }
    }
}