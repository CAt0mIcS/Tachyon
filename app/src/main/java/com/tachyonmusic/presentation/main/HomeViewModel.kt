package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHistory: ObserveHistory,
    getOrLoadArtwork: GetOrLoadArtwork,

    getSavedData: GetSavedData,
    setRepeatMode: SetRepeatMode,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,

    private val playPlayback: PlayPlayback,
    private val unloadArtworks: UnloadArtworks
) : ViewModel() {

    val history = observeHistory().onEach { history ->
        getOrLoadArtwork(history.map { it.underlyingSong }).onEach { res ->
            when (res) {
                is Resource.Loading -> history[res.data!!.i].isArtworkLoading.update { true }
                else -> {
                    history[res.data!!.i].artwork.update { res.data!!.artwork }
                    history[res.data!!.i].isArtworkLoading.update { false }
                }
            }
        }.collect()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())


    init {
        viewModelScope.launch {
            // Make sure browser repeat mode is up to date with saved one
            setRepeatMode(withContext(Dispatchers.IO) { getSavedData().repeatMode })
        }

        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()
            updateSongDatabase()
        }
    }

    fun onItemClicked(playback: Playback) {
        playPlayback(playback)
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
        }
    }
}