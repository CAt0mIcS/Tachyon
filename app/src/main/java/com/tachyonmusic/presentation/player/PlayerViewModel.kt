package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.presentation.player.data.PlaylistInfo
import com.tachyonmusic.presentation.player.data.SeekIncrements
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    getHistory: GetHistory,

    observeSettings: ObserveSettings,

    private val getCurrentPlaybackPos: GetCurrentPosition,
    private val seekToPosition: SeekToPosition,
    getRecentlyPlayed: GetRecentlyPlayed,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playRecentlyPlayed: PlayRecentlyPlayed,
    private val setRepeatMode: SetRepeatMode,
    private val playPlayback: PlayPlayback,

    private val getPlaybackChildren: GetPlaybackChildren,

    private val savePlaybackToPlaylist: SavePlaybackToPlaylist,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val createAndSaveNewPlaylist: CreateAndSaveNewPlaylist,
    observePlaylists: ObservePlaylists,

    private val getOrLoadArtwork: GetOrLoadArtwork
) : ViewModel() {

    /**************************************************************************
     ********** CURRENT PLAYBACK
     *************************************************************************/
    private val _playback = getMediaStates.playback().map {
        it ?: getHistory().firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val playback = _playback.map {
        it!!
    }.onEach { playback ->
        getOrLoadArtwork(playback.underlyingSong).onEach { res ->
            when (res) {
                is Resource.Loading -> playback.isArtworkLoading.update { true }
                else -> {
                    playback.artwork.update { res.data!!.artwork }
                    playback.isArtworkLoading.update { false }
                }
            }
        }.collect()
    }.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Lazily,
        LocalSongImpl(MediaId(""), "", "", 0.ms)
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

    val isPlaying = getMediaStates.playWhenReady()
    val repeatMode = getMediaStates.repeatMode()

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
                playRecentlyPlayed(_playback.value)
            }
        }
    }

    fun nextRepeatMode() {
        setRepeatMode(repeatMode.value.next)
    }

    fun play(playback: SinglePlayback) =
        playPlayback(playback, playInPlaylist = playbackType.value is PlaybackType.Playlist)


    /**************************************************************************
     ********** NEXT PLAYBACK ITEMS / PLAYLIST ITEMS
     *************************************************************************/
    private val associatedPlaylist = getMediaStates.associatedPlaylist()

    val playbackType = combine(_playback, associatedPlaylist) { playback, playlist ->
        if (playlist != null)
            PlaybackType.Playlist.Remote()
        else PlaybackType.build(playback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlaybackType.Song.Local())

    val subPlaybackItems = combine(
        _playback,
        associatedPlaylist,
        repeatMode,
        getMediaStates.sortParameters()
    ) { playback, playlist, repeatMode, sortParams ->
        val children = getPlaybackChildren(playlist ?: playback, repeatMode, sortParams)

        getOrLoadArtwork(children.map { it.underlyingSong }).onEach { res ->
            when (res) {
                is Resource.Loading -> children[res.data!!.i].isArtworkLoading.update { true }
                else -> {
                    children[res.data!!.i].artwork.update { res.data!!.artwork }
                    children[res.data!!.i].isArtworkLoading.update { false }
                }
            }
        }.collect()

        children
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), emptyList())


    /**************************************************************************
     ********** PLAYLIST CONTROLS
     *************************************************************************/
    val playlists = combine(observePlaylists(), _playback) { playlists, currentPlayback ->
        if (currentPlayback == null)
            return@combine emptyList()

        playlists.map { playlist ->
            PlaylistInfo(playlist.name, playlist.hasPlayback(currentPlayback))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
}
