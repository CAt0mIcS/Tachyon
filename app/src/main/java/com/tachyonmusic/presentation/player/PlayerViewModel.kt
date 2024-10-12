package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewPlaylist
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.GetPlaybackChildren
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.RemovePlaybackFromPlaylist
import com.tachyonmusic.domain.use_case.player.SavePlaybackToPlaylist
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.isPredefined
import com.tachyonmusic.presentation.player.data.PlaylistInfo
import com.tachyonmusic.presentation.player.data.SeekIncrements
import com.tachyonmusic.presentation.player.model.PlayerEntity
import com.tachyonmusic.presentation.player.model.toPlayerEntity
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mediaBrowser: MediaBrowserController,
    playbackRepository: PlaybackRepository,
    loadArtworkForPlayback: LoadArtworkForPlayback,
    settingsRepository: SettingsRepository,

    private val getCurrentPlaybackPos: GetCurrentPosition,
    private val seekToPosition: SeekToPosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playPlayback: PlayPlayback,

    private val getPlaybackChildren: GetPlaybackChildren,

    private val savePlaybackToPlaylist: SavePlaybackToPlaylist,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val createAndSaveNewPlaylist: CreateAndSaveNewPlaylist
) : ViewModel() {

    /**************************************************************************
     ********** CURRENT PLAYBACK
     *************************************************************************/
    private val _playback = mediaBrowser.currentPlayback

    val playback = _playback.map {
        it?.let { pb -> loadArtworkForPlayback(pb).toPlayerEntity() }
            ?: PlayerEntity("", "", 0.ms, MediaId.EMPTY, false)
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        PlayerEntity("", "", 0.ms, MediaId.EMPTY, false)
    )

    val shouldShowPlayer = _playback.map {
        it != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)


    /**************************************************************************
     ********** SETTINGS
     *************************************************************************/
    var showMillisecondsInPositionText = SettingsEntity().shouldMillisecondsBeShown
        private set
    var audioUpdateInterval = SettingsEntity().audioUpdateInterval
        private set

    private val _error = MutableStateFlow<UiText?>(null)
    val error: StateFlow<UiText?> = _error

    init {
        settingsRepository.observe().onEach {
            showMillisecondsInPositionText = it.shouldMillisecondsBeShown
            audioUpdateInterval = it.audioUpdateInterval
        }.launchIn(viewModelScope)
    }


    /**************************************************************************
     ********** MEDIA CONTROLS
     *************************************************************************/
    val seekIncrements = settingsRepository.observe().map {
        SeekIncrements(it.seekForwardIncrement, it.seekBackIncrement)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SeekIncrements())

    val isPlaying = mediaBrowser.isPlaying
    val repeatMode = mediaBrowser.repeatMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RepeatMode.All)

    private var recentlyPlayedPos: Duration? = null

    fun getCurrentPosition() = getCurrentPlaybackPos() ?: recentlyPlayedPos ?: 0.ms
    fun seekTo(pos: Duration) = seekToPosition(pos)
    fun seekBack() = seekToPosition(getCurrentPosition() - seekIncrements.value.back)
    fun seekForward() = seekToPosition(getCurrentPosition() + seekIncrements.value.forward)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            recentlyPlayedPos = getRecentlyPlayed()?.position
        }
    }

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else {
            viewModelScope.launch(Dispatchers.IO) {
                val recentlyPlayed = getRecentlyPlayed()
                runOnUiThreadAsync {
                    playPlayback(
                        _playback.value,
                        recentlyPlayed?.position,
                        if (currentPlaylist.value?.isPredefined != false) PlaybackLocation.PREDEFINED_PLAYLIST else PlaybackLocation.CUSTOM_PLAYLIST
                    )
                }
            }
        }
    }

    fun nextRepeatMode() {
        viewModelScope.launch {
            mediaBrowser.setRepeatMode(repeatMode.value.next)
        }
    }

    fun play(entity: PlayerEntity, playbackLocation: PlaybackLocation? = null) {
        viewModelScope.launch {
            val playback = when (playbackType.value) {
                is PlaybackType.Playlist -> // Currently playing a custom playlist (not predefined)
                    currentPlaylist.value?.playbacks
                        ?.find { it.mediaId == entity.mediaId }

                else -> { // Playing predefined playlist
                    when (entity.playbackType) {
                        is PlaybackType.Song ->
                            predefinedPlaylistsRepository.songPlaylist.value
                                .find { it.mediaId == entity.mediaId }

                        is PlaybackType.Remix ->
                            predefinedPlaylistsRepository.remixPlaylist.value
                                .find { it.mediaId == entity.mediaId }

                        else -> TODO("Can't have playlists inside playlists yet")
                    }
                }
            }
            playPlayback(playback, playbackLocation = playbackLocation)
        }
    }


    /**************************************************************************
     ********** NEXT PLAYBACK ITEMS / PLAYLIST ITEMS
     *************************************************************************/
    private val currentPlaylist = mediaBrowser.currentPlaylist

    val playbackType = combine(_playback, currentPlaylist) { playback, playlist ->
        val type = if (playlist?.isPredefined != false)
            playback?.playbackType
        else
            playlist.playbackType
        type ?: PlaybackType.Song.Local()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlaybackType.Song.Local())

    val subPlaybackItems = combine(
        _playback,
        currentPlaylist,
        repeatMode,
        playbackType,
    ) { playback, playlist, repeatMode, playbackType ->
        val children = if (playbackType is PlaybackType.Playlist)
            getPlaybackChildren(playlist)
        else
            getPlaybackChildren(playback, repeatMode, playback?.mediaId)

        children?.map {
            loadArtworkForPlayback(it).toPlayerEntity()
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val recommendedItems =
        combine(
            _playback,
            playbackRepository.songFlow,
            playbackRepository.remixFlow,
            playbackType
        ) { currentPlayback, songs, remixes, playbackType ->
            when (playbackType) {
                is PlaybackType.Song -> remixes.filter { it.mediaId.underlyingMediaId == currentPlayback?.mediaId }
                    .map { loadArtworkForPlayback(it).toPlayerEntity() }

                is PlaybackType.Remix -> {
                    val song = songs.find { it.mediaId == currentPlayback?.songMediaId }
                        ?: return@combine emptyList()
                    listOf(loadArtworkForPlayback(song).toPlayerEntity())
                }

                else -> emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    /**************************************************************************
     ********** PLAYLIST CONTROLS
     *************************************************************************/
    val playlists =
        combine(playbackRepository.playlistFlow, _playback) { playlists, currentPlayback ->
            if (currentPlayback == null)
                return@combine emptyList()

            playlists.map { playlist ->
                PlaylistInfo(playlist.name, playlist.hasPlayback(currentPlayback))
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun editPlaylist(i: Int, shouldAdd: Boolean) {
        viewModelScope.launch {
            if (shouldAdd)
                savePlaybackToPlaylist(_playback.value, i)
            else
                removePlaybackFromPlaylist(_playback.value, i)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = createAndSaveNewPlaylist(name)
            if (res is Resource.Error)
                _error.update { res.message }
        }
    }

    fun removeFromCurrentPlaylist(toRemove: PlayerEntity) {
        viewModelScope.launch {
            removePlaybackFromPlaylist(
                currentPlaylist.value?.playbacks?.find { it.mediaId == toRemove.mediaId },
                currentPlaylist.value
            )
        }
    }
}
