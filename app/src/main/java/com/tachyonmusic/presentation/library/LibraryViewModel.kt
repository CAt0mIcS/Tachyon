package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.SortType
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.util.displayTitle
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

data class SongUiEntity(
    val mediaId: MediaId,
    val title: String,
    val artist: String,
    val displayTitle: String,
    val displaySubtitle: String,
    val artwork: Artwork?
)


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getRepositoryStates: GetRepositoryStates,
    private val playbackRepository: PlaybackRepository,

    private val playPlayback: PlayPlayback,

    private val addSongToExcludedSongs: AddSongToExcludedSongs,
    private val browser: MediaBrowserController,
    private val deletePlayback: DeletePlayback,

    private val metadataExtractor: SongMetadataExtractor,
    private val log: Logger
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

    val itemsToLoad = MutableStateFlow(0..0)

    val items =
        combine(
            songs,
            customizedSongs,
            playlists,
            filterType,
            itemsToLoad
        ) { songs, customizedSongs, playlists, filterType, itemsToLoad ->
            when (filterType) {
                is PlaybackType.Song -> loadArtwork(songs, itemsToLoad)
//                is PlaybackType.Song -> songs
//                is PlaybackType.CustomizedSong -> customizedSongs
//                is PlaybackType.Playlist -> playlists
                else -> emptyList()
            }
        }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())


    private fun loadArtwork(songs: List<Song>, itemsToLoad: ClosedRange<Int>): List<SongUiEntity> {
        return songs.mapIndexed { i, song ->
            if (i in itemsToLoad && song.artwork is EmbeddedArtwork && !song.artwork!!.isLoaded) {
                // Load artwork
                val uri = (song.artwork as EmbeddedArtwork).uri
                song.artwork = EmbeddedArtwork(EmbeddedArtwork.load(uri, metadataExtractor), uri)

                log.debug("Loading artwork for $song")
            } else if (i !in itemsToLoad && song.artwork is EmbeddedArtwork && song.artwork!!.isLoaded) {
                // Unload artwork
                song.artwork = EmbeddedArtwork(null, (song.artwork as EmbeddedArtwork).uri)
                log.debug("Unloading artwork for $song")
            }
            SongUiEntity(
                song.mediaId,
                song.title,
                song.artist,
                song.displayTitle,
                song.artist,
                song.artwork
            )
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(3000.ms)
                itemsToLoad.update { (itemsToLoad.value.first + 1)..(itemsToLoad.value.last + 2) }
                log.debug("Updating items to load ${itemsToLoad.value}")
            }
        }
    }

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
}