package com.tachyonmusic.presentation.player

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.MediaBrowserController
import com.tachyonmusic.domain.use_case.MillisecondsToReadableString
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val browser: MediaBrowserController,
    private val millisecondsToString: MillisecondsToReadableString
) : ViewModel(), MediaBrowserController.EventListener {

    private val updateHandler = Handler(Looper.getMainLooper())
    private val audioUpdateInterval = 100L

    private val _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    private val _currentPosition = mutableStateOf("")
    val currentPosition: State<String> = _currentPosition

    init {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (browser.isPlaying) {
                    _currentPosition.value = millisecondsToString(browser.currentPosition)
                }
                updateHandler.postDelayed(
                    this,
                    audioUpdateInterval
                )
            }
        })

        browser.addListener(this)

        /**
         * [onPlaybackTransition] already happened when [PlayerViewModel] is launched, thus invoke
         * callback here again to update state
         */
        onPlaybackTransition(browser.playback)
    }

    override fun onCleared() {
        browser.removeListener(this)
    }

    override fun onPlaybackTransition(playback: Playback?) {
        Log.d("PlayerViewModel", "onPlaybackTransition to ${playback?.title} - ${playback?.artist}")
        _playbackState.value.title = playback?.title ?: ""
        _playbackState.value.artist = playback?.artist ?: ""
        _playbackState.value.duration = millisecondsToString(playback?.duration)

        val timingData = browser.timingData
        timingData?.addAll(
            listOf(
                TimingData(10000, 20000),
                TimingData(25000, 40000),
                TimingData(45000, 55000),
                TimingData(60000, 70000),
                TimingData(80000, 90000),
                TimingData(100000, 120000),
                TimingData(140000, 155000)
            )
        )
    }
}