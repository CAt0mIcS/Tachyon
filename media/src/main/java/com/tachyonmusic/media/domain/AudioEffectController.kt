package com.tachyonmusic.media.domain

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.model.SoundFrequency
import com.tachyonmusic.core.domain.model.SoundLevel
import kotlinx.coroutines.flow.StateFlow

interface AudioEffectController {
    val playbackParams: StateFlow<PlaybackParameters>
    val bass: StateFlow<Int>
    val virtualizerStrength: StateFlow<Int>
    val reverb: StateFlow<ReverbConfig>

    val bassEnabled: StateFlow<Boolean>
    val virtualizerEnabled: StateFlow<Boolean>
    val equalizerEnabled: StateFlow<Boolean>
    val reverbEnabled: StateFlow<Boolean>

    val numBands: Int
    val maxBandLevel: SoundLevel
    val minBandLevel: SoundLevel
    val presets: List<String>
    val currentPreset: String?
    val bands: StateFlow<List<EqualizerBand>>

    val reverbAudioEffectId: Int?

    fun setPlaybackParameters(value: PlaybackParameters)
    fun setBass(value: Int)
    fun setVirtualizerStrength(value: Int)
    fun setReverb(value: ReverbConfig)

    fun setEqualizerBandLevel(band: Int, level: SoundLevel)
    fun setEqualizerPreset(preset: String)
    fun getEqualizerBandLevel(band: Int): SoundLevel
    fun getEqualizerBandIndex(
        lowerBandFrequency: SoundFrequency,
        upperBandFrequency: SoundFrequency,
        centerFrequency: SoundFrequency
    ): Int?

    fun updateAudioSessionId(audioSessionId: Int)
    fun release()

    /**
     * @return the actual enabled state of the audio effect, since they may be unavailable
     */

    fun setBassEnabled(enabled: Boolean): Boolean
    fun setVirtualizerEnabled(enabled: Boolean): Boolean
    fun setEqualizerEnabled(enabled: Boolean): Boolean
    fun setReverbEnabled(enabled: Boolean): Boolean
}