package com.tachyonmusic.presentation.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.OnUriPermissionsChanged
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.media.util.setArtworkFromResource
import com.tachyonmusic.util.setPlayableState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    observeSettings: ObserveSettings,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,
    onUriPermissionsChanged: OnUriPermissionsChanged,

    @ApplicationContext
    context: Context,

    private val playPlayback: PlayPlayback,
    private val unloadArtworks: UnloadArtworks
) : ViewModel() {

    val history = observeHistory().onEach { history ->
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(history.map { it.underlyingSong }).onEach { res ->
                history.setArtworkFromResource(res)
            }.collect()
        }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())


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

        onUriPermissionsChanged().onEach {
            history.value.setPlayableState(context)
            getOrLoadArtwork(history.value.map { it.underlyingSong }).onEach { res ->
                history.value.setArtworkFromResource(res)
            }.collect()
        }.launchIn(viewModelScope)
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