package com.tachyonmusic.presentation.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.domain.model.Playback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {
    private val _playbacks = mutableStateOf(emptyList<Playback>())
    val playbacks: State<List<Playback>> = _playbacks

    fun onPlaybackClicked(playback: Playback) {
        println("Clicked on ${playback.title} - ${playback.artist}")
    }
}