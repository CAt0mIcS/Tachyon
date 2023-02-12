package com.tachyonmusic.presentation.library

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.GetSongs
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.ObserveLoops
import com.tachyonmusic.domain.use_case.ObservePlaylists
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getSongs: GetSongs,
    observeLoops: ObserveLoops,
    observePlaylists: ObservePlaylists,
    getOrLoadArtwork: GetOrLoadArtwork,
    private val itemClicked: ItemClicked,
    private val application: Application,
    private val log: Logger
) : ViewModel() {

    var songs = listOf<Song>()
        private set
    private var loops = listOf<Loop>()
    private var playlists = listOf<Playlist>()

    private var filterType: PlaybackType = PlaybackType.Song.Local()

    private val _items = mutableStateOf(listOf<Playback>())
    val items: State<List<Playback>> = _items

    init {
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

        viewModelScope.launch(Dispatchers.IO) {
            songs = getSongs()
            runOnUiThreadAsync { _items.value = songs }

            getOrLoadArtwork(getSongs.entities()).onEach {
                if (it is Resource.Success)
                    songs[it.data!!.i].artwork.value = it.data!!.artwork
                else
                    log.debug(it.message?.asString(application) ?: "No message from artwork loader")

                songs[it.data!!.i].isArtworkLoading.value = false
            }.collect()
        }
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