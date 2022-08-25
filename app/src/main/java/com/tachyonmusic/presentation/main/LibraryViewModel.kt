package com.tachyonmusic.presentation.main

import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.media.playback.Loop
import com.tachyonmusic.media.playback.Playback
import com.tachyonmusic.media.playback.Playlist
import com.tachyonmusic.media.playback.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {
    private val _playbacks = mutableStateOf(emptyList<Playback>())
    val playbacks: State<List<Playback>> = _playbacks

    fun onPlaybackClicked(playback: Playback) {
        println("Clicked on ${playback.title} - ${playback.artist}")
    }
}