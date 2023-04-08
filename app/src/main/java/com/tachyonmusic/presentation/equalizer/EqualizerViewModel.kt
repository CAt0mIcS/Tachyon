package com.tachyonmusic.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.media3.common.PlaybackParameters
import com.tachyonmusic.domain.repository.MediaBrowserController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val browser: MediaBrowserController
) : ViewModel() {
    private val _state = MutableStateFlow(1f)
    val state = _state.asStateFlow()

    init {
        _state.update {
            browser.playbackParameters.pitch
        }
        browser.playbackParameters =
            browser.playbackParameters.withSpeed(browser.playbackParameters.pitch)
    }

    fun onStateChanged(state: Float) {
        browser.playbackParameters = PlaybackParameters(state, state)
    }
}


