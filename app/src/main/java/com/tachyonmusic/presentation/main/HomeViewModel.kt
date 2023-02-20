package com.tachyonmusic.presentation.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.main.ObserveHistory
import com.tachyonmusic.domain.use_case.main.UnloadArtworks
import com.tachyonmusic.domain.use_case.main.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.debugPrint
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playPlayback: PlayPlayback,
    observeHistory: ObserveHistory,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,
    private val unloadArtworks: UnloadArtworks,
    getOrLoadArtwork: GetOrLoadArtwork,

    browser: MediaBrowserController
) : ViewModel() {

    private val _history = mutableStateOf(listOf<Playback>())
    val history: State<List<Playback>> = _history


    init {
        observeHistory().map { newHistory ->
            _history.value = newHistory

            getOrLoadArtwork(newHistory.mapNotNull { it.underlyingSong }).onEach {
                if (it is Resource.Success)
                    history.value[it.data!!.i].artwork.value = it.data!!.artwork

                history.value[it.data!!.i].isArtworkLoading.value = false
            }.collect()
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()
            updateSongDatabase()
        }

//        viewModelScope.launch {
//            while(true) {
//                debugPrint("PB: ${browser.playback}")
//                delay(1000.ms)
//            }
//        }
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