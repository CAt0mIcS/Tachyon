package com.tachyonmusic.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.media.domain.AudioEffectController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class EqualizerState(
    val minBandLevel: Int,
    val maxBandLevel: Int,
    val bands: List<Int>
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffectController: AudioEffectController,
    getRepositoryStates: GetRepositoryStates
) : ViewModel() {
    private val _bassBoost = MutableStateFlow(audioEffectController.bass)
    val bassBoost = _bassBoost.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(audioEffectController.virtualizerStrength)
    val virtualizerStrength = _virtualizerStrength.asStateFlow()

    private val _equalizer = MutableStateFlow(
        EqualizerState(
            audioEffectController.minBandLevel,
            audioEffectController.maxBandLevel,
            audioEffectController.bands
        )
    )
    val equalizer = _equalizer.asStateFlow()

    private val _playbackParameters = MutableStateFlow(audioEffectController.playbackParams)
    val playbackParameters = _playbackParameters.asStateFlow()

    private val _reverb = MutableStateFlow(audioEffectController.reverb)
    val reverb = _reverb.asStateFlow()

    init {
        getRepositoryStates.playback().onEach {
            _bassBoost.update { audioEffectController.bass }
            _virtualizerStrength.update { audioEffectController.virtualizerStrength }
            _equalizer.update { it.copy(bands = audioEffectController.bands) }
            _playbackParameters.update { audioEffectController.playbackParams }
            _reverb.update { audioEffectController.reverb }
        }.launchIn(viewModelScope)
    }

    fun setBandLevel(band: Int, level: Int) {
        audioEffectController.equalizerEnabled = true

        if (audioEffectController.equalizerEnabled) {
            audioEffectController.setBandLevel(band, level)

            _equalizer.update { it.copy(bands = audioEffectController.bands) }
        }
    }

    fun setSpeed(speed: Float) {
        audioEffectController.playbackParams =
            audioEffectController.playbackParams.copy(speed = speed)
        _playbackParameters.update { it.copy(speed = speed) }
    }

    fun setPitch(pitch: Float) {
        audioEffectController.playbackParams =
            audioEffectController.playbackParams.copy(pitch = pitch)
        _playbackParameters.update { it.copy(pitch = pitch) }
    }

    fun setVolume(volume: Float) {
        audioEffectController.volumeEnhancerEnabled = true

        if (audioEffectController.volumeEnhancerEnabled) {
            audioEffectController.playbackParams = audioEffectController.playbackParams.copy(
                volume = if (volume <= 1f) volume else volume * 150
            )
            _playbackParameters.update { it.copy(volume = volume) }
        }
    }

    fun setReverb(reverbConfig: ReverbConfig) {
        audioEffectController.reverbEnabled = true

        if (audioEffectController.reverbEnabled) {
            audioEffectController.reverb = reverbConfig
            _reverb.update { reverbConfig }
        }
    }
}


