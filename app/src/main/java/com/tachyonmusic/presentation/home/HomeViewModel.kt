package com.tachyonmusic.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toEntity
import com.tachyonmusic.domain.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.home.GetSavedData
import com.tachyonmusic.domain.use_case.home.UnloadArtworks
import com.tachyonmusic.domain.use_case.home.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.domain.use_case.search.SearchLocation
import com.tachyonmusic.domain.use_case.search.SearchSpotify
import com.tachyonmusic.domain.use_case.search.SearchStoredPlaybacks
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    private val loadArtworkForPlayback: LoadArtworkForPlayback,

    setRepeatMode: SetRepeatMode,
    getSavedData: GetSavedData,
    observeSettings: ObserveSettings,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,

    private val playPlayback: PlayPlayback,

    private val unloadArtworks: UnloadArtworks,

    private val songRepository: SongRepository,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val searchStoredPlaybacks: SearchStoredPlaybacks,
    private val searchSpotify: SearchSpotify,
) : ViewModel() {

    private val historyArtworkLoadingRange = MutableStateFlow(0..0)

    private val historyLoading = MutableStateFlow(true)
    private val databaseUpdating = MutableStateFlow(true)

    private val _history = playbackRepository.historyFlow.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        emptyList()
    )

    val history = combine(
        _history,
        historyArtworkLoadingRange
    ) { history, artworkLoadingRange ->
        loadArtworkForPlayback(history, artworkLoadingRange).map {
            it.toUiEntity()
        }.apply {
            historyLoading.update { false }
        }
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        emptyList()
    )

    private val _searchResults = MutableStateFlow<List<PlaybackUiEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    val isLoading = combine(databaseUpdating, historyLoading) { databaseUpdating, historyLoading ->
        databaseUpdating || historyLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        viewModelScope.launch {
            // Make sure browser repeat mode is up to date with saved one
            setRepeatMode(withContext(Dispatchers.IO) { getSavedData().repeatMode })
        }

        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()

            observeSettings().onEach {
                if (it.musicDirectories.isNotEmpty()) {
                    databaseUpdating.update { true }
                    updateSongDatabase(it)
                    databaseUpdating.update { false }
                }
            }.collect()
        }
    }

    fun onItemClicked(entity: PlaybackUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val playback = _history.value.find { it.mediaId == entity.mediaId }
                ?: return@launch

            if (playback.mediaId.isSpotifySong &&
                !predefinedPlaylistsRepository.songPlaylist.value.contains(playback)
            ) {
                val prevSize = predefinedPlaylistsRepository.songPlaylist.value.size
                songRepository.addAll(listOf((playback as Song).toEntity()))

                while (predefinedPlaylistsRepository.songPlaylist.value.size == prevSize)
                    delay(10.ms)
            }

            playPlayback(playback, playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST)
        }
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
        }
    }

    fun search(searchText: String, searchLocation: SearchLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = when (searchLocation) {
                is SearchLocation.Local -> searchStoredPlaybacks(searchText)
                is SearchLocation.Spotify -> searchSpotify(searchText)
            }
            // TODO: Paging for search
            val loadedArtworkResults = loadArtworkForPlayback(results, 0..results.size + 1).map {
                it.toUiEntity()
            }
            _searchResults.update { loadedArtworkResults }
        }
    }

    fun loadArtwork(range: IntRange) {
        historyArtworkLoadingRange.update { range }
    }
}