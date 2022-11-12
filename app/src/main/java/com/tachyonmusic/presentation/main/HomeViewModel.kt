package com.tachyonmusic.presentation.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.main.GetPlaybacksUseCases
import com.tachyonmusic.domain.use_case.main.ItemClicked
import com.tachyonmusic.domain.use_case.main.LoadAlbumArt
import com.tachyonmusic.domain.use_case.main.SearchStoredPlaybacks
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getPlaybacks: GetPlaybacksUseCases,
    private val loadAlbumArt: LoadAlbumArt,
    private val searchPlaybacks: SearchStoredPlaybacks
) : ViewModel() {

    val songs = getPlaybacks.songs()
    val loops = getPlaybacks.loops()
    val playlists = getPlaybacks.playlists()

    val history = getPlaybacks.history()

    // TODO: Make mutable part private
    val albumArtworkLoading = mutableStateMapOf<Song, Boolean /*isLoading*/>()

    private val _searchResults = mutableStateOf(listOf<Playback>())
    val searchResults: State<List<Playback>> = _searchResults

    private val _searchString = mutableStateOf("")
    val searchString: State<String> = _searchString

    private var albumArtLoaded: Boolean = false

    fun onSearch(text: String) {
        _searchString.value = text

        if (text.isEmpty())
            _searchResults.value = listOf()

        if (!albumArtLoaded) {
            loadArtworkState(
                songs.value.toMutableList()
                    .apply { removeAll(history.value.toSet()) })
            albumArtLoaded = true
        }

        val map = sortedMapOf<Double, Playback>()
        searchPlaybacks(searchString.value).map { res ->

            if (res is Resource.Success) {
                map[res.data!!.second] = res.data!!.first
                _searchResults.value = map.values.toList()
            }

        }.launchIn(viewModelScope)
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)

        // Unload artwork in not used songs to save memory
        // TODO: Don't unload now playing artwork, currently is reloaded again in [PlayerViewModel]
        for (song in songs.value) {
            song.unloadArtwork()
        }
    }

    fun loadArtworkState(items: List<Song>) {
        viewModelScope.launch(Dispatchers.IO) {
            loadAlbumArt(items).map { res ->
                when (res) {
                    is Resource.Loading -> albumArtworkLoading[res.data!!] = true
                    is Resource.Error, is Resource.Success -> albumArtworkLoading[res.data!!] =
                        false
                }
            }.collect()
        }
    }
}