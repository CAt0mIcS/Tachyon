package com.tachyonmusic.presentation.equalizer

import androidx.lifecycle.ViewModel
import com.tachyonmusic.domain.repository.AudioEffectController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class EqualizerState(
    val bass: Int = 0,
    val virtualizerStrength: Int = 0,
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val numBands: Int = 0,
    val minBandLevel: Int = 0,
    val maxBandLevel: Int = 0,
    val enabled: Boolean = true,
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

    init {
        _bandLevels.update {
            List(audioEffectController.numBands) {
                audioEffectController.getBandLevel(it)
            }
        }
    }

    fun setBass(bass: Int) {
        if(!equalizerState.value.enabled)
            return

        audioEffectController.bassEnabled = bass != 0

        if (audioEffectController.bassEnabled) {
            audioEffectController.bass = bass
            _equalizerState.update { it.copy(bass = bass) }
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        if(!equalizerState.value.enabled)
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
        if(!equalizerState.value.enabled)
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

    fun switchEnabled() {
        _equalizerState.update { it.copy(enabled = !it.enabled) }
        audioEffectController.bassEnabled = equalizerState.value.enabled
        audioEffectController.virtualizerEnabled = equalizerState.value.enabled
        audioEffectController.equalizerEnabled = equalizerState.value.enabled
    }
}


