package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.GetMediaStates
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.media.core.sortedBy
import com.tachyonmusic.playback_layers.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    playbackRepository: PlaybackRepository,

    private val addSongToExcludedSongs: AddSongToExcludedSongs,
    private val deletePlayback: DeletePlayback,
) : ViewModel() {

    private val _sortParams = MutableStateFlow(SortParameters())
    val sortParams = _sortParams.asStateFlow()

    private var songs = combine(sortParams, playbackRepository.songFlow) { sort, songs ->
        songs.sortedBy(sort)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var loops = combine(sortParams, playbackRepository.loopFlow) { sort, loops ->
        loops.sortedBy(sort)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var playlists =
        combine(sortParams, playbackRepository.playlistFlow) { sort, playlists ->
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
    }

    fun onItemClicked(playback: Playback) {
    }

    fun excludePlayback(playback: Playback) {
        viewModelScope.launch {
            if (playback is Song)
                addSongToExcludedSongs(playback)
            else
                deletePlayback(playback)
        }
    }
}