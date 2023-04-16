package com.tachyonmusic.presentation.equalizer

import androidx.lifecycle.ViewModel
import com.tachyonmusic.domain.repository.AudioEffectController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class EqualizerState(
    val bass: Int,
    val virtualizerStrength: Int,
    val speed: Float,
    val pitch: Float,
    val numBands: Int,
    val minBandLevel: Int,
    val maxBandLevel: Int,
)

data class ReverbState(
    val roomLevel: Int,
    val roomHFLevel: Int,
    val decayTime: Int,
    val decayHFRatio: Int,
    val reflectionsLevel: Int,
    val reflectionsDelay: Int,
    val reverbLevel: Int,
    val reverbDelay: Int,
    val diffusion: Int,
    val density: Int,
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffectController: AudioEffectController
) : ViewModel() {
    private val _equalizerState = MutableStateFlow(
        EqualizerState(
            audioEffectController.bass,
            audioEffectController.virtualizerStrength,
            audioEffectController.speed,
            audioEffectController.pitch,
            audioEffectController.numBands,
            audioEffectController.minBandLevel,
            audioEffectController.maxBandLevel
        )
    )

    val equalizerState = _equalizerState.asStateFlow()

    private val _bandLevels = MutableStateFlow(listOf<Int>())
    val bandLevels = _bandLevels.asStateFlow()

    private val _reverb = MutableStateFlow(
        ReverbState(
            audioEffectController.roomLevel,
            audioEffectController.roomHFLevel,
            audioEffectController.decayTime,
            audioEffectController.decayHFRatio,
            audioEffectController.reflectionsLevel,
            audioEffectController.reflectionsDelay,
            audioEffectController.reverbLevel,
            audioEffectController.reverbDelay,
            audioEffectController.diffusion,
            audioEffectController.density
        )
    )
    val reverb = _reverb.asStateFlow()

    private val _enabled = MutableStateFlow(true)
    val enabled = _enabled.asStateFlow()

    init {
        _bandLevels.update {
            List(audioEffectController.numBands) {
                audioEffectController.getBandLevel(it)
            }
        }
    }

    fun setBass(bass: Int) {
        if (!enabled.value)
            return

        audioEffectController.bassEnabled = bass != 0

        if (audioEffectController.bassEnabled) {
            audioEffectController.bass = bass
            _equalizerState.update { it.copy(bass = bass) }
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        if (!enabled.value)
            return

        audioEffectController.virtualizerEnabled = strength != 0

        if (audioEffectController.virtualizerEnabled) {
            audioEffectController.virtualizerStrength = strength
            _equalizerState.update { it.copy(virtualizerStrength = strength) }
        }
    }

    fun setSpeed(speed: Float) {
        audioEffectController.speed = speed
        _equalizerState.update { it.copy(speed = speed) }
    }

    fun setPitch(pitch: Float) {
        audioEffectController.pitch = pitch
        _equalizerState.update { it.copy(pitch = pitch) }
    }

    fun setBandLevel(band: Int, level: Int) {
        if (!enabled.value)
            return

        audioEffectController.equalizerEnabled = true

        if (audioEffectController.equalizerEnabled) {
            audioEffectController.setBandLevel(band, level)

            _bandLevels.update { old ->
                List(audioEffectController.numBands) { bandNum ->
                    if (bandNum == band) level
                    else old[bandNum]
                }
            }
        }
    }

    fun setReverb(reverbState: ReverbState) {
        if (!enabled.value)
            return

        audioEffectController.reverbEnabled = true

        if (audioEffectController.reverbEnabled) {
            audioEffectController.roomLevel = reverbState.roomLevel
            audioEffectController.roomHFLevel = reverbState.roomHFLevel
            audioEffectController.decayTime = reverbState.decayTime
            audioEffectController.decayHFRatio = reverbState.decayHFRatio
            audioEffectController.reflectionsLevel = reverbState.reflectionsLevel
            audioEffectController.reflectionsDelay = reverbState.reflectionsDelay
            audioEffectController.reverbLevel = reverbState.reverbLevel
            audioEffectController.reverbDelay = reverbState.reverbDelay
            audioEffectController.diffusion = reverbState.diffusion
            audioEffectController.density = reverbState.density

            _reverb.update { reverbState }
        }
    }

    fun switchEnabled() {
        _enabled.update { !it }
        audioEffectController.bassEnabled = enabled.value && audioEffectController.bass != 0
        audioEffectController.virtualizerEnabled =
            enabled.value && audioEffectController.virtualizerStrength != 0
        audioEffectController.equalizerEnabled = enabled.value
        audioEffectController.reverbEnabled = enabled.value
    }
}


