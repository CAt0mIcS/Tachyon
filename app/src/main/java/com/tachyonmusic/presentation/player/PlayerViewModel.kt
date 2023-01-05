package com.tachyonmusic.presentation.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.main.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.main.NormalizePosition
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.SeekPosition
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.presentation.player.data.PlaybackState
import com.tachyonmusic.presentation.player.data.RepeatMode
import com.tachyonmusic.presentation.player.data.SeekIncrementsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCurrentPosition: GetCurrentPosition,
    val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val seekPosition: SeekPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString,
    private val itemClicked: ItemClicked,
    private val pauseResumePlayback: PauseResumePlayback,
    private val normalizePosition: NormalizePosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val setCurrentPlayback: SetCurrentPlayback,
    getHistory: GetHistory,
    observeSettings: ObserveSettings,
    private val browser: MediaBrowserController
) : ViewModel() {

    val currentPosition: Long
        get() = getCurrentPosition() ?: recentlyPlayedPosition

    private var _repeatMode = mutableStateOf<RepeatMode>(RepeatMode.One)
    val repeatMode: State<RepeatMode> = _repeatMode

    val currentPositionNormalized: Float?
        get() = normalizePosition()

    var recentlyPlayedPositionNormalized: Float = 0f
        private set

    private var recentlyPlayedPosition: Long = 0L

    private var _recentlyPlayed = mutableStateOf<Playback?>(null)
    val recentlyPlayed: State<Playback?> = _recentlyPlayed

    private var _isPlaying = mutableStateOf(browser.isPlaying)
    val isPlaying: State<Boolean> = _isPlaying

    private var _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    private var _seekIncrement = mutableStateOf(SeekIncrementsState())
    val seekIncrement: State<SeekIncrementsState> = _seekIncrement

    private var showMillisecondsInPositionText: Boolean = false

    private val mediaListener = MediaListener()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _recentlyPlayed.value = getHistory().firstOrNull()
            updatePlaybackState(recentlyPlayed.value)

            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPositionNormalized = normalizePosition(
                recentlyPlayed.positionMs,
                recentlyPlayed.durationMs
            )
            recentlyPlayedPosition = recentlyPlayed.positionMs
        }

        observeSettings().map {
            _seekIncrement.value =
                SeekIncrementsState(it.seekForwardIncrementMs, it.seekBackIncrementMs)
            showMillisecondsInPositionText = it.shouldMillisecondsBeShown
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
            artwork = playback.artwork,
            isArtworkLoading = playback.isArtworkLoading,
            children = emptyList()
        )
    }

    private inner class MediaListener : MediaBrowserController.EventListener {
        override fun onPlaybackTransition(playback: Playback?) {
            updatePlaybackState(playback)
            if (playback != null)
                _recentlyPlayed.value = playback
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }
}