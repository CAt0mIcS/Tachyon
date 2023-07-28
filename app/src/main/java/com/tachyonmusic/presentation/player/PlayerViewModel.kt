package com.tachyonmusic.presentation.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.isPredefined
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.presentation.player.data.PlaylistInfo
import com.tachyonmusic.presentation.player.data.SeekIncrements
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    getRepositoryStates: GetRepositoryStates,
    playbackRepository: PlaybackRepository,

    observeSettings: ObserveSettings,

    private val getCurrentPlaybackPos: GetCurrentPosition,
    private val seekToPosition: SeekToPosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val setRepeatMode: SetRepeatMode,
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
    private val _playback = getRepositoryStates.playback().map {
        (it ?: playbackRepository.getHistory().firstOrNull())?.copy()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val playback = _playback.map {
        it!!
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        LocalSong(Uri.EMPTY, MediaId.EMPTY, "", "", 0.ms, false)
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

    init {
        observeSettings().onEach {
            showMillisecondsInPositionText = it.shouldMillisecondsBeShown
            audioUpdateInterval = it.audioUpdateInterval
        }.launchIn(viewModelScope)
    }


    /**************************************************************************
     ********** MEDIA CONTROLS
     *************************************************************************/
    val seekIncrements = observeSettings().map {
        SeekIncrements(it.seekForwardIncrement, it.seekBackIncrement)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SeekIncrements())

    val isPlaying = getRepositoryStates.isPlaying()
    val repeatMode = getRepositoryStates.repeatMode()
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
                    playPlayback(_playback.value, recentlyPlayed?.position)
                }
            }
        }
    }

    fun nextRepeatMode() {
        viewModelScope.launch {
            setRepeatMode(repeatMode.value.next)
        }
    }

    fun play(playback: SinglePlayback, playbackLocation: PlaybackLocation? = null) {
        viewModelScope.launch {
            playPlayback(playback, playbackLocation = playbackLocation)
        }
    }


    /**************************************************************************
     ********** NEXT PLAYBACK ITEMS / PLAYLIST ITEMS
     *************************************************************************/
    private val currentPlaylist = getRepositoryStates.currentPlaylist()

    val playbackType = combine(_playback, currentPlaylist) { playback, playlist ->
        if (playlist?.isPredefined == true)
            PlaybackType.build(playback)
        else
            PlaybackType.build(playlist)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlaybackType.Song.Local())

    val subPlaybackItems = combine(
        _playback,
        currentPlaylist,
        repeatMode,
        playbackType
    ) { playback, playlist, repeatMode, playbackType ->
        getPlaybackChildren(
            if (playbackType is PlaybackType.Playlist) playlist else playback,
            repeatMode
        )
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
                savePlaybackToPlaylist(playback.value, i)
            else
                removePlaybackFromPlaylist(playback.value, i)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            createAndSaveNewPlaylist(name)
        }
    }

    fun removeFromCurrentPlaylist(toRemove: SinglePlayback) {
        viewModelScope.launch {
            removePlaybackFromPlaylist(toRemove, currentPlaylist.value)
        }
    }
}
