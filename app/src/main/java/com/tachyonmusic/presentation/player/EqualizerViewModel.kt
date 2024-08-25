package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.app.R
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.model.SoundLevel
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.domain.AudioEffectController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class EqualizerState(
    val minBandLevel: SoundLevel,
    val maxBandLevel: SoundLevel,
    val bands: List<EqualizerBand>?,
    val presets: List<String>
)

data class PlaybackParametersState(
    val speed: String = "1",
    val pitch: String = "1",
    val volume: Float? = null
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffectController: AudioEffectController,
    private val mediaBrowser: MediaBrowserController
) : ViewModel() {

    val bass = mediaBrowser.currentPlayback.map {
        it?.bassBoost ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val virtualizer = mediaBrowser.currentPlayback.map {
        it?.virtualizerStrength ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val reverb = mediaBrowser.currentPlayback.map {
        it?.reverb ?: ReverbConfig()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ReverbConfig())

    val bassEnabled = audioEffectController.bassEnabled
    val virtualizerEnabled = audioEffectController.virtualizerEnabled
    val equalizerEnabled = audioEffectController.equalizerEnabled
    val reverbEnabled = audioEffectController.reverbEnabled

    val equalizer = combine(
        audioEffectController.equalizerEnabled,
        audioEffectController.bands
    ) { _, bands ->
        EqualizerState(
            audioEffectController.minBandLevel,
            audioEffectController.maxBandLevel,
            bands,
            audioEffectController.presets
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private var _playbackParameters = MutableStateFlow(PlaybackParametersState())
    val playbackParameters = _playbackParameters.asStateFlow()

    val playback = combine(mediaBrowser.currentPlayback, _playbackParameters) { playback, params ->
        TODO()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val selectedReverbText: StateFlow<Int> = reverb.map {
        it.toPresetStringId()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), R.string.nothing)

    val selectedEqualizerText: StateFlow<String> = equalizer.map {
        audioEffectController.currentPreset
            ?: "" // TODO R.string.custom in [audioEffectController.currentPreset]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    init {
        mediaBrowser.currentPlayback.onEach { playback ->
            val uiTextSpeed = playbackParameters.value.speed.toFloatOrNull() ?: return@onEach
            val uiTextPitch = playbackParameters.value.pitch.toFloatOrNull() ?: return@onEach

            if (uiTextSpeed != playback?.playbackParameters?.speed || uiTextPitch != playback.playbackParameters.pitch) {
                _playbackParameters.update {
                    playback?.playbackParameters?.toUiState() ?: PlaybackParametersState()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun setBass(bass: Int?) {
        mediaBrowser.updatePlayback {
            it?.copy(bassBoost = bass ?: 0)
        }
    }

    fun setVirtualizerStrength(strength: Int?) {
        mediaBrowser.updatePlayback {
            it?.copy(virtualizerStrength = strength ?: 0)
        }
    }

    fun setBandLevel(band: Int, level: SoundLevel) {
        TODO()
        if (audioEffectController.setEqualizerEnabled(true)) {
            audioEffectController.setEqualizerBandLevel(band, level)
        }
    }

    fun setEqualizerPreset(preset: String) {
        TODO()
        if (audioEffectController.setEqualizerEnabled(true)) {
            audioEffectController.setEqualizerPreset(preset)
        }
    }

    fun setPlaybackParams(speed: String, pitch: String) {
        _playbackParameters.update { it.copy(speed = speed, pitch = pitch) }

        val speedNum = speed.toFloatOrNull() ?: return
        val pitchNum = pitch.toFloatOrNull() ?: return
        if (speedNum > 0f && pitchNum > 0f) {
            mediaBrowser.updatePlayback {
                it?.copy(playbackParameters = it.playbackParameters.copy(speedNum, pitchNum))
            }
        }
    }

    fun setSpeed(speed: String) {
        _playbackParameters.update { it.copy(speed = speed) }

        val num = speed.toFloatOrNull() ?: return
        if (num > 0f) {
            mediaBrowser.updatePlayback {
                it?.copy(playbackParameters = it.playbackParameters.copy(speed = num))
            }
        }
    }

    fun setPitch(pitch: String) {
        _playbackParameters.update { it.copy(pitch = pitch) }

        val num = pitch.toFloatOrNull() ?: return
        if (num > 0f) {
            mediaBrowser.updatePlayback {
                it?.copy(playbackParameters = it.playbackParameters.copy(pitch = num))
            }
        }
    }

    fun setReverb(reverbConfig: ReverbConfig?) {
        mediaBrowser.updatePlayback {
            it?.copy(reverb = reverbConfig)
        }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        audioEffectController.setBassEnabled(enabled)
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        audioEffectController.setVirtualizerEnabled(enabled)
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        audioEffectController.setEqualizerEnabled(enabled)
    }

    fun setReverbEnabled(enabled: Boolean) {
        audioEffectController.setReverbEnabled(enabled)
    }

    private fun PlaybackParameters.toUiState() = PlaybackParametersState(
        speed.toString(),
        pitch.toString(),
        volume
    )
}


