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
import kotlinx.coroutines.flow.*
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
    private val audioEffectController: AudioEffectController
) : ViewModel() {

    val bassEnabled = audioEffectController.bassEnabled
    val virtualizerEnabled = audioEffectController.virtualizerEnabled
    val equalizerEnabled = audioEffectController.equalizerEnabled
    val reverbEnabled = audioEffectController.reverbEnabled

    val bass = audioEffectController.bass
    val virtualizer = audioEffectController.virtualizerStrength
    val reverb = audioEffectController.reverb

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


    val selectedReverbText: StateFlow<Int> = reverb.map {
        it.toPresetStringId()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), R.string.nothing)

    val selectedEqualizerText: StateFlow<String> = equalizer.map {
        audioEffectController.currentPreset
            ?: "" // TODO R.string.custom in [audioEffectController.currentPreset]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    init {
        audioEffectController.playbackParams.onEach { params ->
            val uiTextSpeed = playbackParameters.value.speed.toFloatOrNull() ?: return@onEach
            val uiTextPitch = playbackParameters.value.pitch.toFloatOrNull() ?: return@onEach

            if (uiTextSpeed != params.speed || uiTextPitch != params.pitch) {
                _playbackParameters.update { _ -> params.toUiState() }
            }
        }.launchIn(viewModelScope)
    }

    fun setBass(bass: Int?) {
        if (audioEffectController.setBassEnabled(true) && bass != null)
            audioEffectController.setBass(bass)
        else
            audioEffectController.setBassEnabled(false)
    }

    fun setVirtualizerStrength(strength: Int?) {
        if (audioEffectController.setVirtualizerEnabled(true) && strength != null)
            audioEffectController.setVirtualizerStrength(strength)
        else
            audioEffectController.setVirtualizerEnabled(false)
    }

    fun setBandLevel(band: Int, level: SoundLevel) {
        if (audioEffectController.setEqualizerEnabled(true)) {
            audioEffectController.setEqualizerBandLevel(band, level)
        }
    }

    fun setEqualizerPreset(preset: String) {
        if (audioEffectController.setEqualizerEnabled(true)) {
            audioEffectController.setEqualizerPreset(preset)
        }
    }

    fun setSpeed(speed: String) {
        _playbackParameters.update { it.copy(speed = speed) }

        val num = speed.toFloatOrNull() ?: return
        if (num > 0f) {
            audioEffectController.setPlaybackParameters(
                audioEffectController.playbackParams.value.copy(speed = num)
            )
        }
    }

    fun setPitch(pitch: String) {
        _playbackParameters.update { it.copy(pitch = pitch) }

        val num = pitch.toFloatOrNull() ?: return
        if (num > 0f) {
            audioEffectController.setPlaybackParameters(
                audioEffectController.playbackParams.value.copy(pitch = num)
            )
        }
    }

    fun setReverb(reverbConfig: ReverbConfig) {
        if (audioEffectController.setReverbEnabled(true))
            audioEffectController.setReverb(reverbConfig)
        else
            audioEffectController.setReverbEnabled(false)
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


