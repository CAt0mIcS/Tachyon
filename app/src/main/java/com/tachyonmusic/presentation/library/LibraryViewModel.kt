package com.tachyonmusic.presentation.library

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.library.SetSortParameters
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    getSongs: GetSongs,
    observeLoops: ObserveLoops,
    observePlaylists: ObservePlaylists,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val setSortParameters: SetSortParameters,
    private val playPlayback: PlayPlayback
) : ViewModel() {

    val sortParams = getMediaStates.sortParameters()

    private var songs = sortParams.map {
        val songs = getSongs(it)
        loadArtwork(songs)
        songs
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var loops = combine(sortParams, observeLoops()) { sort, loops ->
        loadArtwork(loops)
        loops.sortedBy(sort)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var playlists = combine(sortParams, observePlaylists()) { sort, playlists ->
        loadArtwork(playlists)
        playlists.sortedBy(sort)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var _filterType = MutableStateFlow<PlaybackType>(PlaybackType.Song.Local())
    val filterType = _filterType.asStateFlow()

    val items =
        combine(songs, loops, playlists, filterType) { songs, loops, playlists, filterType ->
            when (filterType) {
                is PlaybackType.Song -> songs
                is PlaybackType.Loop -> loops
                is PlaybackType.Playlist -> playlists
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun onFilterSongs() {
        _filterType.value = PlaybackType.Song.Local()
    }

    fun onFilterLoops() {
        _filterType.value = PlaybackType.Loop.Remote()
    }

    fun onFilterPlaylists() {
        _filterType.value = PlaybackType.Playlist.Remote()
    }

    fun onSortTypeChanged(type: SortType) {
        setSortParameters(sortParams.value.copy(type = type))
    }

    fun onItemClicked(playback: Playback) {
        playPlayback(playback)
    }


    private suspend fun loadArtwork(playbacks: List<Playback>) {
        getOrLoadArtwork(playbacks.mapNotNull { it.underlyingSong }).onEach { res ->
            when (res) {
                is Resource.Loading -> playbacks[res.data!!.i].isArtworkLoading.update { true }
                else -> {
                    playbacks[res.data!!.i].artwork.update { res.data!!.artwork }
                    playbacks[res.data!!.i].isArtworkLoading.update { false }
                }
            }
        }.collect()
    }
}