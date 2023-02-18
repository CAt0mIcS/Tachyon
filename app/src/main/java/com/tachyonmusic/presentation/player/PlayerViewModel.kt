package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewPlaylist
import com.tachyonmusic.domain.use_case.player.GetAssociatedPlaylistState
import com.tachyonmusic.domain.use_case.player.GetCurrentPlaybackState
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.GetIsPlayingState
import com.tachyonmusic.domain.use_case.player.GetPlaybackChildren
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayRecentlyPlayed
import com.tachyonmusic.domain.use_case.player.RemovePlaybackFromPlaylist
import com.tachyonmusic.domain.use_case.player.SavePlaybackToPlaylist
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.domain.use_case.player.SetRepeatMode
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.presentation.player.data.PlaylistInfo
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SeekIncrements(
    var forward: Duration = SettingsEntity().seekForwardIncrement,
    var back: Duration = SettingsEntity().seekBackIncrement
)


@HiltViewModel
class PlayerViewModel @Inject constructor(
    getPlaybackState: GetCurrentPlaybackState,
    getHistory: GetHistory,

    observeSettings: ObserveSettings,

    getIsPlayingState: GetIsPlayingState,
    private val getCurrentPlaybackPos: GetCurrentPosition,
    private val seekToPosition: SeekToPosition,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playRecentlyPlayed: PlayRecentlyPlayed,
    private val setRepeatMode: SetRepeatMode,
    private val playPlayback: PlayPlayback,

    getAssociatedPlaylistState: GetAssociatedPlaylistState,
    private val getPlaybackChildren: GetPlaybackChildren,

    private val savePlaybackToPlaylist: SavePlaybackToPlaylist,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val createAndSaveNewPlaylist: CreateAndSaveNewPlaylist,

    private val getOrLoadArtwork: GetOrLoadArtwork
) : ViewModel() {

    /***********************************************************************************************
     ************************************ CURRENT PLAYBACK *****************************************
     **********************************************************************************************/
    private val _playback = getPlaybackState().map {
        it ?: getHistory().firstOrNull() as SinglePlayback?
    }.onEach {
        loadArtworkAsync()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val playback = _playback.map {
        it!!
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        LocalSongImpl(MediaId(""), "", "", 0.ms)
    )

    val shouldShowPlayer = _playback.map {
        it != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)


    /***********************************************************************************************
     ************************************ SETTINGS *************************************************
     **********************************************************************************************/
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


    /***********************************************************************************************
     ************************************ MEDIA CONTROLS *******************************************
     **********************************************************************************************/
    private val _seekIncrements = MutableStateFlow(SeekIncrements())
    val seekIncrements = _seekIncrements.asStateFlow()

    val isPlaying = getIsPlayingState()

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.One)
    val repeatMode = _repeatMode.asStateFlow()

    fun getCurrentPosition() = getCurrentPlaybackPos() ?: 0.ms
    fun seekTo(pos: Duration) = seekToPosition(pos)
    fun seekBack() = seekToPosition(getCurrentPosition() + seekIncrements.value.back)
    fun seekForward() = seekToPosition(getCurrentPosition() - seekIncrements.value.forward)

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else {
            viewModelScope.launch(Dispatchers.IO) {
                playRecentlyPlayed(_playback.value)
            }
        }
    }

    fun nextRepeatMode() {
        _repeatMode.value = repeatMode.value.next
        setRepeatMode(repeatMode.value)
    }

    fun play(playback: SinglePlayback) = playPlayback(playback)


    /***********************************************************************************************
     *************************** NEXT PLAYBACK ITEMS / PLAYLIST ITEMS ******************************
     **********************************************************************************************/
    private val associatedPlaylist = getAssociatedPlaylistState()

    val playbackType = combine(_playback, associatedPlaylist) { playback, playlist ->
        if (playlist != null)
            PlaybackType.Playlist.Remote()
        else PlaybackType.build(playback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlaybackType.Song.Local())

    val subPlaybackItems =
        combine(_playback, associatedPlaylist, repeatMode) { playback, playlist, repeatMode ->
            getPlaybackChildren(playlist ?: playback, repeatMode)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    /***********************************************************************************************
     ************************************ PLAYLIST CONTROL *****************************************
     **********************************************************************************************/
    private val _playlists = MutableStateFlow(emptyList<PlaylistInfo>())
    val playlists = _playlists.asStateFlow()

    fun editPlaylist(i: Int, shouldAdd: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
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


    private fun loadArtworkAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(_playback.value?.underlyingSong ?: return@launch).onEach { res ->
                _playback.value?.artwork?.update { res.data?.artwork }
                _playback.value?.isArtworkLoading?.update { false }
            }.collect()
        }
    }
}