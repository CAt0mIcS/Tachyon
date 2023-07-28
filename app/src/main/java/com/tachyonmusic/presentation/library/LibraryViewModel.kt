package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.playback_layers.SortType
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getRepositoryStates: GetRepositoryStates,
    private val playbackRepository: PlaybackRepository,

    private val playPlayback: PlayPlayback,

    private val addSongToExcludedSongs: AddSongToExcludedSongs,
    private val deletePlayback: DeletePlayback,
) : ViewModel() {

    val sortParams = getRepositoryStates.sortPrefs()

    private var songs = playbackRepository.songFlow.map { songs ->
        songs.filter { !it.isHidden }.map { it.copy() }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var customizedSongs = playbackRepository.customizedSongFlow.map { customizedSongs ->
        customizedSongs.map {
            it.copy()
        }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var playlists = playbackRepository.playlistFlow.map { playlists ->
        playlists.map {
            it.copy()
        }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())

    private var _filterType = MutableStateFlow<PlaybackType>(PlaybackType.Song.Local())
    val filterType = _filterType.asStateFlow()

    val items =
        combine(
            songs,
            customizedSongs,
            playlists,
            filterType
        ) { songs, customizedSongs, playlists, filterType ->
            when (filterType) {
                is PlaybackType.Song -> songs
                is PlaybackType.CustomizedSong -> customizedSongs
                is PlaybackType.Playlist -> playlists
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun onFilterSongs() {
        _filterType.value = PlaybackType.Song.Local()
    }

    fun onFilterCustomizedSongs() {
        _filterType.value = PlaybackType.CustomizedSong.Local()
    }

    fun onFilterPlaylists() {
        _filterType.value = PlaybackType.Playlist.Local()
    }

    fun onSortTypeChanged(type: SortType) {
        // TODO: UseCase
        playbackRepository.setSortingPreferences(sortParams.value.copy(type = type))
    }

    fun onItemClicked(playback: Playback) {
        viewModelScope.launch {
            playPlayback(playback, playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST)
        }
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