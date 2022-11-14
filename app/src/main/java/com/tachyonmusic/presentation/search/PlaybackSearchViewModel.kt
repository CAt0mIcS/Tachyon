package com.tachyonmusic.presentation.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.playback.Playback
import com.tachyonmusic.core.data.playback.Song
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.search.SearchStoredPlaybacks
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackSearchViewModel @Inject constructor(
    getSongs: GetSongs,
    private val itemClicked: ItemClicked,
    private val loadPlaybackArtwork: LoadPlaybackArtwork,
    private val searchStoredPlaybacks: SearchStoredPlaybacks
) : ViewModel() {

    private val songs = getSongs().value

    // TODO: Make mutable part private
    val albumArtworkLoading = mutableStateMapOf<Song, Boolean /*isLoading*/>()

    private val _searchResults = mutableStateOf(listOf<Playback>())
    val searchResults: State<List<Playback>> = _searchResults

    private var albumArtLoaded: Boolean = false

    fun onSearch(query: String?) {
        if (query == null || query.isEmpty())
            _searchResults.value = listOf()

        if (!albumArtLoaded) {
            viewModelScope.launch(Dispatchers.IO) {
                loadPlaybackArtwork(songs).map { res ->
                    when (res) {
                        is Resource.Loading -> albumArtworkLoading[res.data!!] = true
                        is Resource.Error, is Resource.Success -> albumArtworkLoading[res.data!!] =
                            false
                    }
                }.collect()
            }
            albumArtLoaded = true
        }

        val map = sortedMapOf<Double, Playback>()
        searchStoredPlaybacks(query).map { res ->

            if (res is Resource.Success) {
                map[res.data!!.second] = res.data!!.first
                _searchResults.value = map.values.toList()
            }

        }.launchIn(viewModelScope)
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)

        // TODO: Unload artwork in not used songs to save memory
        // TODO: Don't unload now playing artwork, currently is reloaded again in [PlayerViewModel]
//        for (song in songs) {
//            song.unloadArtwork()
//        }
    }
}