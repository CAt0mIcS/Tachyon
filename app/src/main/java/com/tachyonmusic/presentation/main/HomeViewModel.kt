package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.main.GetPagedHistory
import com.tachyonmusic.domain.use_case.main.UnloadArtworks
import com.tachyonmusic.domain.use_case.main.UpdateArtworks
import com.tachyonmusic.domain.use_case.main.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getHistory: GetPagedHistory,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,
    private val updateArtworks: UpdateArtworks,
    private val unloadArtworks: UnloadArtworks,
) : ViewModel() {

    var history = getHistory(5)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()
            updateSongDatabase()
            updateArtworks()
        }
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
            updateArtworks(shouldUpdate = true)
        }
    }
}