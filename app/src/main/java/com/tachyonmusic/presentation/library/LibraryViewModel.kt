package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdView
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.domain.use_case.library.AssignArtworkToPlayback
import com.tachyonmusic.domain.use_case.library.QueryArtworkForPlayback
import com.tachyonmusic.domain.use_case.library.UpdatePlaybackMetadata
import com.tachyonmusic.playback_layers.SortType
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.core_components.model.PlaybackUiEntity
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

private const val ADD_INSERT_INTERVAL = 10

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,

    private val playPlayback: PlayPlayback,
    private val browser: MediaBrowserController,

    private val addSongToExcludedSongs: AddSongToExcludedSongs,
    private val deletePlayback: DeletePlayback,

    private val loadArtworkForPlayback: LoadArtworkForPlayback,
    private val queryArtworkForPlayback: QueryArtworkForPlayback,
    private val assignArtworkToPlayback: AssignArtworkToPlayback,

    private val updatePlaybackMetadata: UpdatePlaybackMetadata
) : ViewModel() {

    val sortParams = playbackRepository.sortingPreferences

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

    private var _queriedArtwork = MutableStateFlow(emptyList<Artwork>())
    val queriedArtwork = _queriedArtwork.asStateFlow()

    private var _artworkLoadingError = MutableStateFlow<UiText?>(null)
    val artworkLoadingError = _artworkLoadingError.asStateFlow()

    private val artworkLoadingRange = MutableStateFlow(0..10)

    private val _cachedBannerAds = mutableMapOf<MediaId, AdView>()
    val cachedBannerAds: Map<MediaId, AdView> = _cachedBannerAds

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

                else -> emptyList()
            }.insertBeforeEvery(
                ADD_INSERT_INTERVAL
            ) {
                PlaybackUiEntity(
                    mediaId = MediaId("AD$it"),
                    playbackType = PlaybackType.Ad.Banner()
                )
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
        val rangeToUpdate = removeAdIndices(0..21)
//        val rangeToUpdate = removeAdIndices(range)
        artworkLoadingRange.update { rangeToUpdate }
    }

    /**
     * Starts loading all artwork we can find for [playback] into [queriedArtwork]
     */
    fun queryArtwork(playback: PlaybackUiEntity, searchQuery: String? = null) {
        _queriedArtwork.update { emptyList() }
        queryArtworkForPlayback(playback, searchQuery).onEach { res ->
            if (res is Resource.Success)
                _queriedArtwork.update { (it + res.data!!).copy() }
            else if (res is Resource.Error)
                _artworkLoadingError.update { res.message!! }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun assignArtworkToPlayback(artwork: Artwork, playback: PlaybackUiEntity) {
        viewModelScope.launch {
            assignArtworkToPlayback(playback.mediaId, artwork)
        }
    }

    fun updateMetadata(playback: PlaybackUiEntity, title: String?, artist: String?, name: String?) {
        viewModelScope.launch {
            updatePlaybackMetadata(playback.mediaId, title, artist, name)
        }
    }

    fun cacheAd(mediaId: MediaId, ad: AdView) {
        _cachedBannerAds[mediaId] = ad
    }

    private fun PlaybackUiEntity.toPlayback() = when (playbackType) {
        is PlaybackType.Song -> songs.value.find { it.mediaId == mediaId }
        is PlaybackType.CustomizedSong -> customizedSongs.value.find { it.mediaId == mediaId }
        is PlaybackType.Playlist -> playlists.value.find { it.mediaId == mediaId }
        is PlaybackType.Ad -> null
    }

    private fun <E> List<E>.insertBeforeEvery(insertBeforeIdx: Int, elem: (i: Int) -> E): List<E> {
        val result = mutableListOf<E>()
        for (i in indices) {
            if (i % insertBeforeIdx == 0)
                result.add(elem(i))
            result.add(this[i])
        }
        return result
    }

    /**
     * The range received from the UI includes all banner ad indices which we don't need for ranged
     * loading of playback artwork. Function removes these unwanted indices
     */
    private fun removeAdIndices(range: IntRange): IntRange {
        val numAds = range.last / ADD_INSERT_INTERVAL + 1
        return (range.first + numAds)..(range.last + numAds)
    }
}