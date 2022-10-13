package com.tachyonmusic.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.media.data.ext.parcelable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
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
                    updateStates()
                }
                updateHandler.postDelayed(
                    this,
                    audioUpdateInterval
                )
            }
        })

        browser.registerEventListener(this)

        val initialState: Bundle? = savedStateHandle[MetadataKeys.Playback]
        if (initialState == null) {
            Log.d("PlayerViewModel", "No saved playback state present, loading default media item")
            // Playback launched from LibraryScreen

            /**
             * [onPlaybackTransition] already happened when [PlayerViewModel] is launched, thus invoke
             * callback here again to update state.
             */
            onPlaybackTransition(browser.playback)
        } else {
            Log.d("PlayerViewModel", "Loading playback from saved state handle")
            // TODO: Shouldn't use [parcelable] function here, as it should probably be private in project media
            browser.playback = initialState.parcelable(MetadataKeys.Playback)

            // TODO: Nothing happening after setting playback here, also playback is already set in browser.playback (preserved?)
        }

        /**
         * Callback called when [savedStateHandle] needs to save in order to preserve state.
         * Saving the current playback to be able to load it again once the activity is recreated
         */
        savedStateHandle.setSavedStateProvider(MetadataKeys.Playback) {
            Log.d("PlayerViewModel", "Placing playback into saved state")
            Bundle().apply {
                putParcelable(MetadataKeys.Playback, browser.playback)
            }
        }
    }

    override fun onCleared() {
        browser.unregisterEventListener(this)
    }

    override fun onPlaybackTransition(playback: Playback?) {
        Log.d("PlayerViewModel", "onPlaybackTransition to ${playback?.title} - ${playback?.artist}")
        _playbackState.value.title = playback?.title ?: ""
        _playbackState.value.artist = playback?.artist ?: ""
        _playbackState.value.durationString = millisecondsToString(playback?.duration)
        _playbackState.value.duration = playback?.duration ?: 0L

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

    fun updateStates() {
        _currentPosition.value = millisecondsToString(browser.currentPosition)
    }
}