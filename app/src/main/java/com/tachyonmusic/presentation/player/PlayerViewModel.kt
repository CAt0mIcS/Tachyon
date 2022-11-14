package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.domain.use_case.player.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val millisecondsToReadableString: MillisecondsToReadableString
) : ViewModel() {

    val isPlaying = playerListener.isPlaying
    val currentPosition: Long
        get() = getCurrentPosition()
    val audioUpdateInterval: Duration
        get() = getAudioUpdateInterval()

    val playbackState = handlePlaybackState.playbackState
    val loopState = handleLoopState.loopState


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

    fun onSaveLoop(name: String) {
        viewModelScope.launch {
            val loop = createAndSaveNewLoop(name)
            if(loop.data == null)
                TODO("Error: Invalid loop")
            setCurrentPlayback(loop.data)
        }
    }

    fun onAddTimingData() {
        handleLoopState.onNewTimingData()
    }

    fun onTimingDataValuesChanged(i: Int, startTime: Long, endTime: Long) {
        handleLoopState.onTimingDataValuesChanged(i, startTime, endTime)
    }

    fun onSetUpdatedTimingData() {
        handleLoopState.onSetUpdatedTimingData()
    }
}