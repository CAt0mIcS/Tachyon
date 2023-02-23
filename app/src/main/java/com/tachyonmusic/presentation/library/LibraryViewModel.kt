package com.tachyonmusic.presentation.library

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.GetSongs
import com.tachyonmusic.domain.use_case.ObserveLoops
import com.tachyonmusic.domain.use_case.ObservePlaylists
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getSongs: GetSongs,
    observeLoops: ObserveLoops,
    observePlaylists: ObservePlaylists,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val playPlayback: PlayPlayback,
    private val application: Application,
    private val log: Logger
) : ViewModel() {

    private var songs = listOf<Song>()
    private var loops = listOf<Loop>()
    private var playlists = listOf<Playlist>()

    private var _filterType = MutableStateFlow<PlaybackType>(PlaybackType.Song.Local())
    val filterType = _filterType.asStateFlow()

    private val _items = MutableStateFlow(listOf<Playback>())
    val items = _items.asStateFlow()

    init {
        observeLoops().map {
            loops = it
            loadArtwork(loops)
            if (filterType.value is PlaybackType.Loop) {
                _items.value = emptyList()
                _items.value = loops
            }
        }.launchIn(viewModelScope)

        observePlaylists().map {
            playlists = it
            loadArtwork(playlists)
            if (filterType.value is PlaybackType.Playlist) {
                _items.value = emptyList()
                _items.value = playlists
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            songs = getSongs()
            _items.update { songs }
            loadArtwork(songs)
        }
    }


    fun onFilterSongs() {
        _items.value = songs
        _filterType.value = PlaybackType.Song.Local()
    }

    fun onFilterLoops() {
        _items.value = loops
        _filterType.value = PlaybackType.Loop.Remote()
    }

    fun onFilterPlaylists() {
        _items.value = playlists
        _filterType.value = PlaybackType.Playlist.Remote()
    }

    fun onItemClicked(playback: Playback) {
        playPlayback(playback)
    }


    private suspend fun loadArtwork(playbacks: List<Playback>) {
        getOrLoadArtwork(playbacks.mapNotNull { it.underlyingSong }).onEach { res ->
            if (res is Resource.Success)
                playbacks[res.data!!.i].artwork.update { res.data!!.artwork }
            else
                log.debug(res.message?.asString(application) ?: "No message from artwork loader")

            playbacks[res.data!!.i].isArtworkLoading.update { false }
        }.collect()
    }
}