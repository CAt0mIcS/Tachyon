package com.tachyonmusic.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.home.GetSavedData
import com.tachyonmusic.domain.use_case.home.UnloadArtworks
import com.tachyonmusic.domain.use_case.home.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.domain.use_case.search.SearchLocation
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.use_case.SearchStoredPlaybacks
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    settingsRepository: SettingsRepository,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,

    private val playPlayback: PlayPlayback,

    private val unloadArtworks: UnloadArtworks,

    private val searchStoredPlaybacks: SearchStoredPlaybacks,

    private val log: Logger
) : ViewModel() {

    private val historyArtworkLoadingRange = MutableStateFlow(0..0)
    private var cachedMusicDirectories = emptyList<Uri>()

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
        }
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        emptyList()
    )

    private val _searchResults = MutableStateFlow<List<PlaybackUiEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            // Make sure browser repeat mode is up to date with saved one
            setRepeatMode(withContext(Dispatchers.IO) { getSavedData().repeatMode })
        }

        viewModelScope.launch(Dispatchers.IO) {
            cachedMusicDirectories = settingsRepository.getSettings().musicDirectories
            updateSettingsDatabase()

            settingsRepository.observe().onEach {
                if (it.musicDirectories.isNotEmpty() && cachedMusicDirectories != it.musicDirectories) {
                    log.info("Starting song database update due to new music directory or reload")
                    updateSongDatabase(it)
                    cachedMusicDirectories = it.musicDirectories
                }
            }.collect()
        }
    }

    fun onItemClicked(entity: PlaybackUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val playback = _history.value.find { it.mediaId == entity.mediaId }
                ?: return@launch

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