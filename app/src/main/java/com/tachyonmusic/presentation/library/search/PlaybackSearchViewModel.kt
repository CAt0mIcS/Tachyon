package com.tachyonmusic.presentation.library.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.model.SearchLocation
import com.tachyonmusic.domain.use_case.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.media.domain.use_case.PlaybackSearchResult
import com.tachyonmusic.media.domain.use_case.SearchStoredPlaybacks
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toPlaylist
import com.tachyonmusic.presentation.core_components.model.toRemix
import com.tachyonmusic.presentation.core_components.model.toSong
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import com.tachyonmusic.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

// TODO: Highlights from [PlaybackSearchResult.*highlightIndices]
//    https://stackoverflow.com/questions/68981311/is-there-a-way-to-change-the-background-color-of-a-specific-word-in-outlined-tex
data class SearchResultUiEntity(
    val playback: PlaybackUiEntity,
    val score: Float
)


@HiltViewModel
class PlaybackSearchViewModel @Inject constructor(
    private val searchStoredPlaybacks: SearchStoredPlaybacks,
    private val playbackRepository: PlaybackRepository,
    private val playPlayback: PlayPlayback,
    private val loadArtworkForPlayback: LoadArtworkForPlayback
) : ViewModel() {
    private var playbackType: PlaybackType = PlaybackType.Song.Local()

    private var _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // TODO: Only load visible?
    private var itemDisplayRange = MutableStateFlow(10)

    private var _searchLocation = MutableStateFlow<SearchLocation>(SearchLocation.Local)
    val searchLocation = _searchLocation.asStateFlow()

    val searchResults =
        combine(searchQuery, searchLocation, itemDisplayRange) { query, location, itemRange ->
            when (location) {
                SearchLocation.Local -> {
                    val quality = 50
                    loadArtwork(
                        searchStoredPlaybacks(query, playbackType, itemRange),
                        0..Int.MAX_VALUE,
                        quality
                    ).map {
                        SearchResultUiEntity(
                            it.playback.toUiEntity(),
                            it.score
                        )
                    } // TODO: playback type
                }
            }.copy()
        }.debounce(500L).stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )


    fun search(query: String, playbackType: PlaybackType) {
        this.playbackType = playbackType
        _searchQuery.update { query }
    }

    fun updateSearchLocation(location: SearchLocation) {
        _searchLocation.update { location }
    }

    fun increaseLoadingRange() {
        itemDisplayRange.update { it + 10 }
    }

    fun resetLoadingRange() {
        itemDisplayRange.update { 10 }
    }

    fun onItemClicked(entity: PlaybackUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            playPlayback(
                entity.toPlayback(),
                playbackLocation = PlaybackLocation.PREDEFINED_PLAYLIST
            )
        }
    }

    private suspend fun PlaybackUiEntity.toPlayback() = when (playbackType) {
        is PlaybackType.Song -> toSong(playbackRepository.getSongs())
        is PlaybackType.Remix -> toRemix(playbackRepository.getRemixes())
        is PlaybackType.Playlist -> toPlaylist(playbackRepository.getPlaylists())
        is PlaybackType.Ad -> TODO("Cannot convert Ad to Playback")
    }

    private fun loadArtwork(
        searches: List<PlaybackSearchResult>,
        range: IntRange,
        quality: Int
    ): List<PlaybackSearchResult> {
        val artworks = loadArtworkForPlayback(searches.map { it.playback }, range, quality)
        return artworks.mapIndexed { i, loadedPlayback ->
            searches[i].apply {
                when (playback) {
                    is SinglePlayback -> {
                        (playback as SinglePlayback).artwork =
                            (loadedPlayback as SinglePlayback).artwork
                    }

                    is Playlist -> {
                        // TODO
                    }
                }
            }
        }
    }
}