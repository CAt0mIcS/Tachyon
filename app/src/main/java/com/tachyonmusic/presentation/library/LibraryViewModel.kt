package com.tachyonmusic.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.ObserveLoops
import com.tachyonmusic.domain.use_case.ObservePlaylists
import com.tachyonmusic.domain.use_case.ObserveSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeSongs: ObserveSongs,
    observeLoops: ObserveLoops,
    observePlaylists: ObservePlaylists,
    private val itemClicked: ItemClicked
) : ViewModel() {

    private var songs = listOf<Song>()
    private var loops = listOf<Loop>()
    private var playlists = listOf<Playlist>()

    private var _items = mutableStateOf<List<Playback>>(songs)
    val items: State<List<Playback>> = _items

    init {
        observeSongs().map {
            songs = it
            if(items.value.isEmpty())
                onFilterSongs()
        }.launchIn(viewModelScope)

        observeLoops().map {
            loops = it
        }.launchIn(viewModelScope)

        observePlaylists().map {
            playlists = it
        }.launchIn(viewModelScope)
    }


    fun onFilterSongs() {
        _items.value = songs
    }

    fun onFilterLoops() {
        _items.value = loops
    }

    fun onFilterPlaylists() {
        _items.value = playlists
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }
}