package com.tachyonmusic.media.data

import android.media.audiofx.BassBoost
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.model.SoundFrequency
import com.tachyonmusic.core.domain.model.SoundLevel
import com.tachyonmusic.core.domain.model.mDb
import com.tachyonmusic.core.domain.model.mHz
import com.tachyonmusic.media.domain.AudioEffectController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * TODO: Changes in audio effects from e.g. the MediaPlaybackService (when switching to loop for example) are not always reflected in the UI due
 *      to no states being used for audio effect variables (bass, reverb, ...)
 */

class AndroidAudioEffectController : AudioEffectController {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var environmentalReverb: EnvironmentalReverb? = null

    /**************************************************************************
     ********** Enabled states
     *************************************************************************/

    private var _bassEnabled = MutableStateFlow(false)
    override val bassEnabled = _bassEnabled.asStateFlow()

    private var _virtualizerEnabled = MutableStateFlow(false)
    override val virtualizerEnabled = _virtualizerEnabled.asStateFlow()

    private var _equalizerEnabled = MutableStateFlow(false)
    override val equalizerEnabled = _equalizerEnabled.asStateFlow()

    private var _reverbEnabled = MutableStateFlow(false)
    override val reverbEnabled = _reverbEnabled.asStateFlow()

    override val bassValue: Int?
        get() = if(bassEnabled.value) bass.value else null
    override val virtualizerValue: Int?
        get() = if(virtualizerEnabled.value) virtualizerStrength.value else null
    override val reverbValue: ReverbConfig?
        get() = if(reverbEnabled.value) reverb.value else null
    override val equalizerBandValues: List<EqualizerBand>?
        get() = if(equalizerEnabled.value) bands.value else null

    /**************************************************************************
     ********** [PlaybackParameters]
     *************************************************************************/

    private var _playbackParams = MutableStateFlow(PlaybackParameters())
    override val playbackParams = _playbackParams.asStateFlow()

    /**************************************************************************
     ********** Bass
     *************************************************************************/

    private var _bass = MutableStateFlow(0) // TODO: Default value
    override val bass = _bass.asStateFlow()

    /**************************************************************************
     ********** Virtualizer
     *************************************************************************/

    private var _virtualizerStrength = MutableStateFlow(0) // TODO: Default value
    override val virtualizerStrength = _virtualizerStrength.asStateFlow()

    /**************************************************************************
     ********** Reverb |
     *************************************************************************/
    private var _reverb = MutableStateFlow(ReverbConfig())
    override var reverb = _reverb.asStateFlow()

    /**************************************************************************
     ********** Equalizer
     *************************************************************************/

    override val numBands: Int
        get() = if (!equalizerEnabled.value) 0 else equalizer?.numberOfBands?.toInt() ?: 0

    override val minBandLevel: SoundLevel
        get() = if (!equalizerEnabled.value) 0.mDb else equalizer?.bandLevelRange?.first()?.mDb
            ?: 0.mDb

    override val maxBandLevel: SoundLevel
        get() = if (!equalizerEnabled.value) 0.mDb else equalizer?.bandLevelRange?.last()?.mDb
            ?: 0.mDb

    override val presets: List<String>
        get() = List(equalizer?.numberOfPresets?.toInt() ?: 0) {
            equalizer?.getPresetName(it.toShort())
        }.filterNotNull()

    override val currentPreset: String?
        get() {
            val name = equalizer?.getPresetName(equalizer?.currentPreset ?: return null)
            return if (name?.isBlank() == true) "Custom" else name
        }

    private var _bands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    override val bands = _bands.asStateFlow()
    override val reverbAudioEffectId: Int?
        get() = if (reverbEnabled.value) environmentalReverb?.id else null

    override fun setPlaybackParameters(value: PlaybackParameters) {
        // TODO: Volume
        _playbackParams.update { value }
    }

    override fun setBass(value: Int) {
        assert(bassEnabled.value) { "Bass is not enabled" }
        bassBoost!!.setStrength(value.toShort())
        _bass.update { bassBoost!!.roundedStrength.toInt() }
    }

    override fun setVirtualizerStrength(value: Int) {
        assert(virtualizerEnabled.value) { "Virtualizer is not enabled" }
        virtualizer!!.setStrength(value.toShort())
        _virtualizerStrength.update { virtualizer!!.roundedStrength.toInt() }
    }

    override fun setReverb(value: ReverbConfig) {
        assert(reverbEnabled.value) { "Reverb is not enabled" }
        environmentalReverb!!.roomLevel = value.roomLevel
        environmentalReverb!!.roomHFLevel = value.roomHFLevel
        environmentalReverb!!.decayTime = value.decayTime
        environmentalReverb!!.decayHFRatio = value.decayHFRatio
        environmentalReverb!!.reflectionsLevel = value.reflectionsLevel
        environmentalReverb!!.reflectionsDelay = value.reflectionsDelay
        environmentalReverb!!.reverbLevel = value.reverbLevel
        environmentalReverb!!.reverbDelay = value.reverbDelay
        environmentalReverb!!.diffusion = value.diffusion
        environmentalReverb!!.density = value.density
        _reverb.update { value }
    }


    override fun setEqualizerBandLevel(band: Int, level: SoundLevel) {
        assert(level in minBandLevel..maxBandLevel) { "BandLevel $level is invalid (range: $minBandLevel..$maxBandLevel)" }
        assert(band in 0..numBands) { "Band $band is invalid (max: ${numBands - 1})" }
        equalizer?.setBandLevel(band.toShort(), level.inmDb.toShort())

        val newBands = loadNewEqualizerBands()
        _bands.update { newBands }
    }

    override fun setEqualizerPreset(preset: String) {
        assert(preset in presets) { "Preset $preset not found in available presets" }
        equalizer?.usePreset(presets.indexOf(preset).toShort())

        val newBands = loadNewEqualizerBands()
        _bands.update { newBands }
    }

    override fun getEqualizerBandLevel(band: Int): SoundLevel {
        assert(band in 0..numBands) { "Band $band is invalid (max: ${numBands - 1})" }
        return equalizer?.getBandLevel(band.toShort())?.mDb ?: 0.mDb
    }

    override fun getEqualizerBandIndex(
        lowerBandFrequency: SoundFrequency,
        upperBandFrequency: SoundFrequency,
        centerFrequency: SoundFrequency
    ): Int? {
        val idx = bands.value.indexOfFirst {
            it.lowerBandFrequency == lowerBandFrequency && it.upperBandFrequency == upperBandFrequency && it.centerFrequency == centerFrequency
        }
        if (idx == -1)
            return null
        return idx
    }

    // TODO: DynamicProcessing, HapticGenerator
    // TODO: Release audio effects

    override fun updateAudioSessionId(audioSessionId: Int) {
        equalizer = Equalizer(Int.MAX_VALUE, audioSessionId)
        virtualizer = Virtualizer(Int.MAX_VALUE, audioSessionId)
        bassBoost = BassBoost(Int.MAX_VALUE, audioSessionId)
        environmentalReverb = EnvironmentalReverb(0, 0)
    }

    override fun release() {
        equalizer?.release()
        virtualizer?.release()
        bassBoost?.release()
        environmentalReverb?.release()
    }

    override fun setBassEnabled(enabled: Boolean): Boolean {
        bassBoost?.enabled = enabled
        _bassEnabled.update { bassBoost?.enabled == true && bassBoost?.hasControl() == true }
        return bassEnabled.value
    }

    override fun setVirtualizerEnabled(enabled: Boolean): Boolean {
        virtualizer?.enabled = enabled
        _virtualizerEnabled.update { virtualizer?.enabled == true && virtualizer?.hasControl() == true && virtualizer?.strengthSupported == true }
        return virtualizerEnabled.value
    }

    override fun setEqualizerEnabled(enabled: Boolean): Boolean {
        equalizer?.enabled = enabled
        _equalizerEnabled.update { equalizer?.enabled == true && equalizer?.hasControl() == true }

        val newBands = loadNewEqualizerBands()
        _bands.update { newBands }

        return equalizerEnabled.value
    }

    override fun setReverbEnabled(enabled: Boolean): Boolean {
        environmentalReverb?.enabled = enabled
        _reverbEnabled.update { environmentalReverb?.enabled == true && environmentalReverb?.hasControl() == true }
        return reverbEnabled.value
    }


    private fun loadNewEqualizerBands(): List<EqualizerBand> = List(numBands) {
        val range = equalizer?.getBandFreqRange(it.toShort()) ?: return emptyList()
        EqualizerBand(
            level = getEqualizerBandLevel(it),
            lowerBandFrequency = range.first().mHz,
            upperBandFrequency = range.last().mHz,
            centerFrequency = equalizer?.getCenterFreq(it.toShort())?.mHz ?: return emptyList()
        )
    }
}