package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import com.tachyonmusic.domain.use_case.player.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerListener: PlayerListenerHandler,
    private val getCurrentPosition: GetCurrentPosition,
    private val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val handlePlaybackState: HandlePlaybackState,
    private val handleLoopState: HandleLoopState,
    private val seekToPosition: SeekToPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString
) : ViewModel() {

    val isPlaying = playerListener.isPlaying
    val currentPosition: Long
        get() = getCurrentPosition()
    val audioUpdateInterval: Duration
        get() = getAudioUpdateInterval()

    val playbackState = handlePlaybackState.playbackState


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
}