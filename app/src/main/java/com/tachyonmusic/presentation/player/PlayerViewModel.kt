package com.tachyonmusic.presentation.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.player.PlayerUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val browser: MediaBrowserController,
    private val useCases: PlayerUseCases
) : ViewModel(), MediaBrowserController.EventListener {

    private val updateHandler = Handler(Looper.getMainLooper())
    private val audioUpdateInterval = 100L

    private val _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    private val _currentPosition = mutableStateOf(UpdateState())
    val currentPosition: State<UpdateState> = _currentPosition

    private val _loopState = mutableStateOf(LoopState())
    val loopState: State<LoopState> = _loopState

    private var isSeeking = false

    init {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (browser.isPlaying && !isSeeking) {
                    updateStates()
                }
                updateHandler.postDelayed(
                    this,
                    audioUpdateInterval
                )
            }
        })

        browser.registerEventListener(this)

        /**
         * [onPlaybackTransition] already happened when [PlayerViewModel] is launched, thus invoke
         * callback here again to update state.
         * Or in case the system killed the activity, reassign the states using the playback which
         * withstands the activity being killed
         */
        onPlaybackTransition(browser.playback)
    }

    override fun onCleared() {
        browser.unregisterEventListener(this)
    }

    override fun onPlaybackTransition(playback: Playback?) {
        Log.d("PlayerViewModel", "onPlaybackTransition to ${playback?.title} - ${playback?.artist}")
        _playbackState.value.title = playback?.title ?: ""
        _playbackState.value.artist = playback?.artist ?: ""
        _playbackState.value.durationString = useCases.millisecondsToString(playback?.duration)
        _playbackState.value.duration = playback?.duration ?: 0L

        _loopState.value = LoopState(
            playback?.timingData?.getOrNull(0)?.startTime ?: 0L,
            playback?.timingData?.getOrNull(0)?.endTime ?: playback?.duration ?: 0L
        )

        val timingData = browser.timingData
//        timingData?.addAll(
//            listOf(
//                TimingData(10000, 20000), // 10s - 20s
//                TimingData(25000, 40000), // 25s - 40s
//                TimingData(45000, 55000), // 45s - 55s
//                TimingData(80000, 90000), // 1:20 - 1:30
//                TimingData(60000, 70000), // 1min - 1:10
//                TimingData(100000, 120000), // 1:40 - 2min
//                TimingData(140000, 155000) // 2:20 - 2:35
//            )
//        )

//        timingData?.addAll(
//            listOf(
//                TimingData(10000, 20000), // 10s - 20s
//                TimingData(18000, 40000), // 18s - 40s
//                TimingData(36000, 55000), // 36s - 55s
//                TimingData(180000, 5000), // 3min - 5s
//            )
//        )

//        timingData?.addAll(
//            listOf(
//                TimingData(18000, 40000), // 18s - 40s
//                TimingData(36000, 55000), // 36s - 55s
//                TimingData(10000, 20000), // 10s - 20s
//            )
//        )
    }

    fun onPositionChange(pos: Long) {
        isSeeking = true
        updateStates(pos)
    }

    fun onPositionChangeFinished() {
        browser.seekTo(currentPosition.value.pos)
        isSeeking = false
    }

    fun onLoopStateChanged(startTime: Long, endTime: Long) {
        _loopState.value = LoopState(startTime, endTime)
    }

    fun onLoopStateChangeFinished() {
        if(loopState.value.startTime == 0L && loopState.value.endTime == currentPosition.value.pos) {
            browser.timingData?.clear()
            return
        }

        val data = TimingData(loopState.value.startTime, loopState.value.endTime)
        if ((browser.timingData?.size ?: 0) > 0)
            browser.timingData?.set(0, data)
        else
            browser.timingData?.add(data)
    }

    fun updateStates(pos: Long? = null) {
        _currentPosition.value = UpdateState(
            pos ?: browser.currentPosition ?: 0L,
            useCases.millisecondsToString(pos ?: browser.currentPosition)
        )
    }
}