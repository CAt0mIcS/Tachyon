package com.tachyonmusic.presentation.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.library.SetSortParameters
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.media.util.setArtworkFromResource
import com.tachyonmusic.util.setPlayableState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    observeSongs: ObserveSongs,
    onUriPermissionsChanged: OnUriPermissionsChanged,
    observeLoops: ObserveLoops,
    observePlaylists: ObservePlaylists,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val setSortParameters: SetSortParameters,
    private val playPlayback: PlayPlayback,

    @ApplicationContext
    context: Context
) : ViewModel() {

    val sortParams = getMediaStates.sortParameters()

    private var songs = combine(sortParams, observeSongs()) { sort, songs ->
        val newSongs = songs.sortedBy(sort)
        loadArtworkAsync(newSongs)
        newSongs
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var loops = combine(sortParams, observeLoops()) { sort, loops ->
        val newLoops = loops.sortedBy(sort)
        loadArtworkAsync(newLoops)
        newLoops
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var playlists = combine(sortParams, observePlaylists()) { sort, playlists ->
        val newPlaylists = playlists.sortedBy(sort)
        loadArtworkAsync(newPlaylists)
        newPlaylists
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


    init {
        onUriPermissionsChanged().onEach {
            songs.value.setPlayableState(context)
            loadArtworkAsync(songs.value)

            loops.value.setPlayableState(context)
            loadArtworkAsync(loops.value)
        }.launchIn(viewModelScope)
    }


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


    private suspend fun loadArtworkAsync(playbacks: List<Playback>) {
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(playbacks.mapNotNull { it.underlyingSong }).onEach { res ->
                playbacks.setArtworkFromResource(res)
            }.collect()
        }
    }
}