package com.tachyonmusic.presentation.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.domain.model.Playback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
) : ViewModel() {
    val playbacks = mutableStateListOf<Playback>()

    fun onPlaybackClicked(playback: Playback) {
        println("Clicked on ${playback.title} - ${playback.artist}")
//        browser.playback = playback
    }
}