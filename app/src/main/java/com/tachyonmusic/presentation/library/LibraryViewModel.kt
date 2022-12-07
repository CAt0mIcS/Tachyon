package com.tachyonmusic.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.GetLoops
import com.tachyonmusic.domain.use_case.GetPlaylists
import com.tachyonmusic.domain.use_case.GetSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getSongs: GetSongs,
    getLoops: GetLoops,
    getPlaylists: GetPlaylists,
) : ViewModel() {

    private var songs = emptyList<Song>()
    private val loops = getLoops()
    private val playlists = getPlaylists()

    private val _items = mutableStateOf<List<Playback>>(emptyList())
    val items: State<List<Playback>> = _items

    init {
        viewModelScope.launch(Dispatchers.IO) {
            songs = getSongs()
            onFilterSongs()
        }
    }


    fun onFilterSongs() {
        _items.value = songs
    }

    fun onFilterLoops() {
        _items.value = loops.value
    }

    fun onFilterPlaylists() {
        _items.value = playlists.value
    }
}