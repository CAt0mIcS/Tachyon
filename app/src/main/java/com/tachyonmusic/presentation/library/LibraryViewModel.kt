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
import com.tachyonmusic.presentation.util.SortOrder
import com.tachyonmusic.presentation.util.SortType
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThreadAsync
import com.tachyonmusic.util.sortedBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SortParameters(
    val type: SortType,
    val order: SortOrder
)


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

    private var _sortParams =
        MutableStateFlow(SortParameters(SortType.AlphabeticalTitle, SortOrder.Ascending))
    val sortParams = _sortParams.asStateFlow()

    private var songs = sortParams.map {
        val songs = getSongs(it.type, it.order)
        loadArtwork(songs)
        songs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var loops = combine(sortParams, observeLoops()) { sort, loops ->
        loadArtwork(loops)
        loops.sortedBy(sort.type, sort.order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var playlists = combine(sortParams, observePlaylists()) { sort, playlists ->
        loadArtwork(playlists)
        playlists.sortedBy(sort.type, sort.order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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
        _sortParams.value = sortParams.value.copy(type = type)
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