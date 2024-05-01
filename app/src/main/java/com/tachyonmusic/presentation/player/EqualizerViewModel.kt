package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffectController: AudioEffectController,
    mediaBrowser: MediaBrowserController
) : ViewModel() {
    private val _bassBoost = MutableStateFlow(audioEffectController.bass)
    val bassBoost = _bassBoost.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(audioEffectController.virtualizerStrength)
    val virtualizerStrength = _virtualizerStrength.asStateFlow()

    private val _equalizer = MutableStateFlow(
        EqualizerState(
            audioEffectController.minBandLevel,
            audioEffectController.maxBandLevel,
            audioEffectController.bands,
            audioEffectController.presets
        )
    )
    val equalizer = _equalizer.asStateFlow()

    private val _playbackParameters = MutableStateFlow(audioEffectController.playbackParams)
    val playbackParameters = _playbackParameters.asStateFlow()

    private val _reverb = MutableStateFlow(audioEffectController.reverb)
    val reverb = _reverb.asStateFlow()

    init {
        mediaBrowser.currentPlayback.onEach {
            _bassBoost.update { audioEffectController.bass }
            _virtualizerStrength.update { audioEffectController.virtualizerStrength }
            _equalizer.update { it.copy(bands = audioEffectController.bands) }
            _playbackParameters.update { audioEffectController.playbackParams }
            _reverb.update { audioEffectController.reverb }
        }.launchIn(viewModelScope)
    }

    fun setBass(bass: Int?) {
        if (audioEffectController.bassEnabled) {
            audioEffectController.bass = bass
            _bassBoost.update { audioEffectController.bass }
        } else
            _bassBoost.update { null }
    }

    fun setVirtualizerStrength(strength: Int?) {
        if (audioEffectController.virtualizerEnabled) {
            audioEffectController.virtualizerStrength = strength
            _virtualizerStrength.update { audioEffectController.virtualizerStrength }
        } else
            _virtualizerStrength.update { null }
    }

    fun setBandLevel(band: Int, level: SoundLevel) {
        if (audioEffectController.equalizerEnabled) {
            audioEffectController.setBandLevel(band, level)

            _equalizer.update { it.copy(bands = audioEffectController.bands) }
        }
    }

    fun setEqualizerPreset(preset: String) {
        if(audioEffectController.equalizerEnabled) {
            audioEffectController.setPreset(preset)

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
        if (audioEffectController.reverbEnabled) {
            audioEffectController.reverb = reverbConfig
            _reverb.update { reverbConfig }
        } else
            _reverb.update { null }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        audioEffectController.bassEnabled = enabled
        _bassBoost.update { audioEffectController.bass }
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        audioEffectController.virtualizerEnabled = enabled
        _virtualizerStrength.update { audioEffectController.virtualizerStrength }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        audioEffectController.equalizerEnabled = enabled
        _equalizer.update {
            EqualizerState(
                audioEffectController.minBandLevel,
                audioEffectController.maxBandLevel,
                audioEffectController.bands,
                audioEffectController.presets
            )
        }
    }

    fun setReverbEnabled(enabled: Boolean) {
        audioEffectController.reverbEnabled = enabled
        _reverb.update { audioEffectController.reverb }
    }
}


