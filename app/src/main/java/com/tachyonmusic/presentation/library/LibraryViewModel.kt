package com.tachyonmusic.presentation.library

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
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

    private var filterType: PlaybackType = PlaybackType.Song.Local()

    private val _items = mutableStateOf(listOf<Playback>())
    val items: State<List<Playback>> = _items

    init {
        observeSongs().map {
            songs = it
            if (filterType is PlaybackType.Song.Local) {
                _items.value = emptyList()
                _items.value = songs
            }
        }.launchIn(viewModelScope)

        observeLoops().map {
            loops = it
            if (filterType is PlaybackType.Loop.Remote) {
                _items.value = emptyList()
                _items.value = loops
            }
        }.launchIn(viewModelScope)

        observePlaylists().map {
            playlists = it
            if (filterType is PlaybackType.Playlist.Remote) {
                _items.value = emptyList()
                _items.value = playlists
            }
        }.launchIn(viewModelScope)
    }


    fun onFilterSongs() {
        _items.value = songs
        filterType = PlaybackType.Song.Local()
    }

    fun onFilterLoops() {
        _items.value = loops
        filterType = PlaybackType.Loop.Remote()
    }

    fun onFilterPlaylists() {
        _items.value = playlists
        filterType = PlaybackType.Playlist.Remote()
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }
}