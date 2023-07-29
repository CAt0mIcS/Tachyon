package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.LoadArtworkForPlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.playback_layers.SortType
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toUiEntity
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
    private val browser: MediaBrowserController,
    private val deletePlayback: DeletePlayback,

    private val loadArtworkForPlayback: LoadArtworkForPlayback
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

    private val artworkLoadingRange = MutableStateFlow(0..10)

    val items =
        combine(
            songs,
            customizedSongs,
            playlists,
            filterType,
            artworkLoadingRange
        ) { songs, customizedSongs, playlists, filterType, itemsToLoad ->
            val quality = 50
            when (filterType) {
                is PlaybackType.Song -> loadArtworkForPlayback(songs, itemsToLoad, quality).map {
                    it.toUiEntity()
                }
                is PlaybackType.CustomizedSong -> loadArtworkForPlayback(
                    customizedSongs,
                    itemsToLoad,
                    quality
                ).map {
                    it.toUiEntity()
                }
                is PlaybackType.Playlist -> loadArtworkForPlayback(
                    playlists,
                    itemsToLoad,
                    quality
                ).map {
                    it.toUiEntity()
                }
            }
        }.stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )


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

    fun onItemClicked(entity: PlaybackUiEntity) {
        viewModelScope.launch {
            playPlayback(
                entity.toPlayback(),
                playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST
            )
        }
    }

    fun excludePlayback(entity: PlaybackUiEntity) {
        viewModelScope.launch {
            val playback = entity.toPlayback()
            if (playback is Song)
                addSongToExcludedSongs(playback)
            else {
                if (browser.currentPlayback.value == playback)
                    if ((browser.currentPlaylist.value?.playbacks?.size ?: 0) > 1)
                        browser.seekToNext()
                    else
                        browser.stop()
                deletePlayback(playback)
            }
        }
    }

    fun loadArtwork(range: IntRange) {
        artworkLoadingRange.update { range }
    }

    private fun PlaybackUiEntity.toPlayback() = when (playbackType) {
        is PlaybackType.Song -> songs.value.find { it.mediaId == mediaId }
        is PlaybackType.CustomizedSong -> customizedSongs.value.find { it.mediaId == mediaId }
        is PlaybackType.Playlist -> playlists.value.find { it.mediaId == mediaId }
    }
}