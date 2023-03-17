package com.tachyonmusic.presentation.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.playback_layers.PlaybackRepository
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
    getMediaStates: GetMediaStates,
    playbackRepository: PlaybackRepository,

    observeSettings: ObserveSettings,
    observeSavedData: ObserveSavedData,

    private val getCurrentPlaybackPos: GetCurrentPosition,
    private val seekToPosition: SeekToPosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playPlayback: PlayPlayback,

    private val savePlaybackToPlaylist: SavePlaybackToPlaylist,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val createAndSaveNewPlaylist: CreateAndSaveNewPlaylist,
) : ViewModel() {

    /**************************************************************************
     ********** CURRENT PLAYBACK
     *************************************************************************/
    private val _playback = getMediaStates.playback().map {
        it ?: playbackRepository.getHistory().firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val playback = _playback.map {
        it!!
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        LocalSongImpl(Uri.EMPTY, MediaId(""), "", "", 0.ms)
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

    val isPlaying = getMediaStates.isPlaying()
    val repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.All)
//    val repeatMode =
//        combine(getMediaStates.repeatMode(), observeSavedData()) { browserRepeatMode, savedData ->
//            browserRepeatMode ?: savedData.repeatMode
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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

    }

    fun play(playback: SinglePlayback) {}


    /**************************************************************************
     ********** NEXT PLAYBACK ITEMS / PLAYLIST ITEMS
     *************************************************************************/
    private val associatedPlaylist = getMediaStates.currentPlaylist()

    val playbackType = combine(_playback, associatedPlaylist) { playback, playlist ->
        PlaybackType.build(playlist ?: playback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlaybackType.Song.Local())

    val subPlaybackItems = combine(
        getMediaStates.playback(),
        associatedPlaylist,
        repeatMode
    ) { playback, playlist, repeatMode ->
//        getPlaybackChildren(
//            playlist ?: playback ?: playbackRepository.getHistory().firstOrNull(),
//            repeatMode,
//            SortParameters()
//        )
        emptyList<SinglePlayback>()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())


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
//        viewModelScope.launch {
//            removePlaybackFromPlaylist(toRemove, associatedPlaylist.value)
//            playPlayback(associatedPlaylist.value)
//        }
    }
}
