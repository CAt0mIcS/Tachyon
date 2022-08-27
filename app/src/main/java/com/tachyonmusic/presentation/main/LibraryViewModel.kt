package com.tachyonmusic.presentation.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.domain.playback.Playback
import dagger.hilt.android.lifecycle.HiltViewModel
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