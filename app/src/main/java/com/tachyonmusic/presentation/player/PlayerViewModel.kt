package com.tachyonmusic.presentation.player

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.presentation.player.data.RepeatMode
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.LoadPlaybackArtwork
import com.tachyonmusic.domain.use_case.player.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Long.max
import javax.inject.Inject
import kotlin.math.min
import kotlin.time.Duration


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerListener: PlayerListenerHandler,
    private val getCurrentPosition: GetCurrentPosition,
    private val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val handlePlaybackState: HandlePlaybackState,
    private val handleLoopState: HandleLoopState,
    private val seekToPosition: SeekToPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString,
    private val itemClicked: ItemClicked,
    private val pauseResumePlayback: PauseResumePlayback,
) : ViewModel(), MediaBrowserController.EventListener {

    val isPlaying = playerListener.isPlaying

    val currentPosition: Long
        get() = getCurrentPosition()
    val audioUpdateInterval: Duration
        get() = getAudioUpdateInterval()

    val playbackState = handlePlaybackState.playbackState
    val loopState = handleLoopState.loopState

    private var _repeatMode = mutableStateOf<RepeatMode>(RepeatMode.One)
    val repeatMode: State<RepeatMode> = _repeatMode


    fun registerPlayerListeners() {
        handlePlaybackState.register()
        handleLoopState.register()
        playerListener.register()
    }

    fun unregisterPlayerListeners() {
        handlePlaybackState.unregister()
        handleLoopState.unregister()
        playerListener.unregister()
    }

    fun getTextForPosition(position: Long) = millisecondsToReadableString(position)

    fun onSeekTo(position: Long) {
        seekToPosition(position)
    }

    fun onItemClicked(playback: Playback?) {
        itemClicked(playback)
    }

    // TODO: Don't hard-code 10000 back/forward seek time, should be a user setting
    fun onSeekBack() {
        seekToPosition(max(currentPosition - 10000, 0L))
    }

    fun onSeekForward() {
        seekToPosition(min(currentPosition + 10000, playbackState.value.duration))
    }

    fun pause() {
        pauseResumePlayback(PauseResumePlayback.Action.Pause)
    }

    fun resume() {
        pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun onRepeatModeChange() {
        _repeatMode.value = repeatMode.value.next
    }
}