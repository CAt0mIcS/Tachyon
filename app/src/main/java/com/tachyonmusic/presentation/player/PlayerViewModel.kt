package com.tachyonmusic.presentation.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateObject
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

    val loopState = mutableStateListOf<TimingData>()

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

        /**
         * Adding timing data back after activity is killed
         */
        loopState.clear()
        loopState.addAll(browser.timingData ?: listOf())
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
    }

    fun onPositionChange(pos: Long) {
        isSeeking = true
        updateStates(pos)
    }

    fun onPositionChangeFinished() {
        browser.seekTo(currentPosition.value.pos)
        isSeeking = false
    }

    fun onLoopStateChanged(i: Int, startTime: Long, endTime: Long) {
        loopState[i] = TimingData(startTime, endTime)
    }

    fun onLoopStateChangeFinished() {
        browser.timingData = loopState
    }

    fun onAddNewTimingData() {
        if (browser.timingData != null) {
            val data = TimingData(0L, browser.duration ?: return)
            loopState.add(data)
            browser.timingData!!.add(data)
        }
    }

    private fun updateStates(pos: Long? = null) {
        _currentPosition.value = UpdateState(
            pos ?: browser.currentPosition ?: 0L,
            useCases.millisecondsToString(pos ?: browser.currentPosition)
        )
    }
}