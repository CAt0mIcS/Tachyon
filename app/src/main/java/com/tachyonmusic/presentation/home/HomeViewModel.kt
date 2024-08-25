package com.tachyonmusic.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.home.UnloadArtworks
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    playbackRepository: PlaybackRepository,
    private val loadArtworkForPlayback: LoadArtworkForPlayback,
    mediaBrowser: MediaBrowserController,
    private val dataRepository: DataRepository,
    private val playPlayback: PlayPlayback,
) : ViewModel() {

    private val historyArtworkLoadingRange = MutableStateFlow(0..0)

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

    init {
        viewModelScope.launch {
            // Make sure browser repeat mode is up to date with saved one
            mediaBrowser.setRepeatMode(withContext(Dispatchers.IO) { dataRepository.getData().repeatMode })
        }
    }

    fun onItemClicked(entity: PlaybackUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val playback = _history.value.find { it.mediaId == entity.mediaId }
                ?: return@launch

            playPlayback(playback, playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST)
        }
    }

    fun debugAction() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.update(maxRemixCount = 0)
        }
    }

    fun loadArtwork(range: IntRange) {
        historyArtworkLoadingRange.update { range }
    }
}