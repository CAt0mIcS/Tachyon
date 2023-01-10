package com.tachyonmusic.presentation.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.main.NormalizePosition
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.SeekPosition
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.presentation.player.data.ArtworkState
import com.tachyonmusic.presentation.player.data.PlaybackState
import com.tachyonmusic.presentation.player.data.RepeatMode
import com.tachyonmusic.presentation.player.data.SeekIncrementsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCurrentPosition: GetCurrentPosition,
    private val seekPosition: SeekPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString,
    private val itemClicked: ItemClicked,
    private val pauseResumePlayback: PauseResumePlayback,
    private val normalizePosition: NormalizePosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val browser: MediaBrowserController,  // TODO: Shouldn't be used in ViewModel
    getHistory: GetHistory,
    observeSettings: ObserveSettings,
) : ViewModel() {

    private var _repeatMode = mutableStateOf<RepeatMode>(RepeatMode.One)
    val repeatMode: State<RepeatMode> = _repeatMode

    private var _recentlyPlayed = mutableStateOf<Playback?>(null)
    val recentlyPlayed: State<Playback?> = _recentlyPlayed

    private var _isPlaying = mutableStateOf(browser.isPlaying)
    val isPlaying: State<Boolean> = _isPlaying

    private var _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    private var _artworkState = mutableStateOf(ArtworkState())
    val artworkState: State<ArtworkState> = _artworkState

    private var _seekIncrement = mutableStateOf(SeekIncrementsState())
    val seekIncrement: State<SeekIncrementsState> = _seekIncrement

    var audioUpdateInterval: Duration = 100.milliseconds
        private set

    val currentPosition: Long
        get() = getCurrentPosition() ?: recentlyPlayedPosition

    val currentPositionNormalized: Float?
        get() = normalizePosition()

    var recentlyPlayedPositionNormalized: Float = 0f
        private set

    private var recentlyPlayedPosition: Long = 0L
    private var showMillisecondsInPositionText: Boolean = false
    private val mediaListener = MediaListener()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            _recentlyPlayed.value = getHistory().firstOrNull()
            updatePlaybackState(recentlyPlayed.value)
            if (recentlyPlayed.value != null)
                getOrLoadArtworkForPlayback(recentlyPlayed.value!!)


            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPositionNormalized = normalizePosition(
                recentlyPlayed?.positionMs ?: 0L,
                recentlyPlayed?.durationMs ?: 0L
            )
            recentlyPlayedPosition = recentlyPlayed?.positionMs ?: 0L
        }

        observeSettings().map {
            _seekIncrement.value =
                SeekIncrementsState(it.seekForwardIncrementMs, it.seekBackIncrementMs)
            showMillisecondsInPositionText = it.shouldMillisecondsBeShown
            audioUpdateInterval = it.audioUpdateInterval.milliseconds
        }.launchIn(viewModelScope)
    }

    fun registerMediaListener() {
        browser.registerEventListener(mediaListener)
    }

    fun unregisterMediaListener() {
        browser.unregisterEventListener(mediaListener)
    }

    fun getTextForPosition(position: Long) =
        millisecondsToReadableString(position, showMillisecondsInPositionText)

    fun onSeekTo(position: Long) {
        seekPosition(position)
    }

    fun onItemClicked(playback: Playback?) {
        itemClicked(playback)
    }

    fun onSeekBack() {
        seekPosition(currentPosition - seekIncrement.value.backward)
    }

    fun onSeekForward() {
        seekPosition(currentPosition + seekIncrement.value.forward)
    }

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else if (!setCurrentPlaybackToRecentlyPlayed(playWhenReady = true))
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun nextRepeatMode() {
        _repeatMode.value = repeatMode.value.next
    }

    fun setCurrentPlaybackToRecentlyPlayed(
        playWhenReady: Boolean = false
    ): Boolean = setCurrentPlayback(recentlyPlayed.value, playWhenReady, recentlyPlayedPosition)

    private fun updatePlaybackState(playback: Playback?) {
        if (playback == null)
            return

        _playbackState.value = PlaybackState(
            title = playback.title ?: "Unknown Title",
            artist = playback.artist ?: "Unknown Artist",
            duration = playback.duration ?: 0L,
            children = emptyList()
        )
    }

    private fun updateArtworkState(playback: Playback?) {
        if (playback == null)
            return

        _artworkState.value =
            ArtworkState(artwork = playback.artwork, isArtworkLoading = playback.isArtworkLoading)
    }

    private fun getOrLoadArtworkForPlayback(playback: Playback) {
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(playback).onEach {
                recentlyPlayed.value?.artwork?.value = it.data?.artwork
                recentlyPlayed.value?.isArtworkLoading?.value = false
                updateArtworkState(recentlyPlayed.value)
            }.collect()
        }
    }

    private inner class MediaListener : MediaBrowserController.EventListener {
        override fun onPlaybackTransition(playback: Playback?) {
            updatePlaybackState(playback)
            if (playback != null) {
                _recentlyPlayed.value = playback
                getOrLoadArtworkForPlayback(playback)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }
}