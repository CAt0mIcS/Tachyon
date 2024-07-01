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

/**
 * TODO: Changes in audio effects from e.g. the MediaPlaybackService (when switching to loop for example) are not always reflected in the UI due
 *      to no states being used for audio effect variables (bass, reverb, ...)
 */

class AndroidAudioEffectController : AudioEffectController {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var environmentalReverb: EnvironmentalReverb? = null
    private var volumeEnhancer: LoudnessEnhancer? = null

    override var controller: AudioEffectController.PlaybackController? = null

    /**************************************************************************
     ********** Bass
     *************************************************************************/

    override var bass: Int?
        get() = if (!bassEnabled) null else bassBoost?.roundedStrength?.toInt()
        set(value) {
            if (value == null) {
                bassEnabled = false
                return
            }
            bassBoost?.setStrength(value.toShort())
        }

    /**************************************************************************
     ********** Virtualizer
     *************************************************************************/

    override var virtualizerStrength: Int?
        get() = if (!virtualizerEnabled) null else virtualizer?.roundedStrength?.toInt()
        set(value) {
            if (value == null) {
                virtualizerEnabled = false
                return
            }
            virtualizer?.setStrength(value.toShort())
        }

    /**************************************************************************
     ********** Enabled states
     *************************************************************************/

    override var bassEnabled: Boolean
        get() = bassBoost?.enabled == true && bassBoost?.hasControl() == true
        set(value) {
            bassBoost?.enabled = value
        }

    override var virtualizerEnabled: Boolean
        get() = virtualizer?.enabled == true && virtualizer?.hasControl() == true && virtualizer?.strengthSupported == true
        set(value) {
            virtualizer?.enabled = value
        }

    override var equalizerEnabled: Boolean
        get() = equalizer?.enabled == true && equalizer?.hasControl() == true
        set(value) {
            equalizer?.enabled = value
        }

    override var reverbEnabled: Boolean
        get() = environmentalReverb?.enabled == true && environmentalReverb?.hasControl() == true
        set(value) {
            environmentalReverb?.enabled = value
            controller?.onReverbToggled(value, environmentalReverb?.id ?: return)
        }

    override var volumeEnhancerEnabled: Boolean
        get() = volumeEnhancer?.enabled == true && volumeEnhancer?.hasControl() == true
        set(value) {
            volumeEnhancer?.enabled = value
        }

    /**************************************************************************
     ********** [PlaybackParameters]
     *************************************************************************/

    override var playbackParams = PlaybackParameters(1f, 1f, 1f)
        set(value) {
            field = value
            if (value.volume > 1f) {
                controller?.onNewPlaybackParameters(value.copy(volume = 1f))
                volumeEnhancer?.setTargetGain((value.volume - 1).toInt())
            } else {
                controller?.onNewPlaybackParameters(value)
                volumeEnhancer?.setTargetGain(0)
            }
        }

    /**************************************************************************
     ********** Equalizer
     *************************************************************************/

    override val numBands: Int
        get() = if (!equalizerEnabled) 0 else equalizer?.numberOfBands?.toInt() ?: 0

    override val minBandLevel: SoundLevel
        get() = if (!equalizerEnabled) 0.mDb else equalizer?.bandLevelRange?.first()?.mDb ?: 0.mDb

    override val maxBandLevel: SoundLevel
        get() = if (!equalizerEnabled) 0.mDb else equalizer?.bandLevelRange?.last()?.mDb ?: 0.mDb

    override val presets: List<String>
        get() = List(equalizer?.numberOfPresets?.toInt() ?: 0) {
            equalizer?.getPresetName(it.toShort())
        }.filterNotNull()

    override val bands: List<EqualizerBand>?
        get() {
            if (!equalizerEnabled)
                return null
            else
                return List(numBands) {
                    val range = equalizer?.getBandFreqRange(it.toShort()) ?: return null
                    EqualizerBand(
                        level = getBandLevel(it),
                        lowerBandFrequency = range.first().mHz,
                        upperBandFrequency = range.last().mHz,
                        centerFrequency = equalizer?.getCenterFreq(it.toShort())?.mHz ?: return null
                    )
                }
        }

    /**************************************************************************
     ********** Reverb |
     *************************************************************************/
    override var reverb: ReverbConfig?
        get() = if (!reverbEnabled || environmentalReverb == null) null else ReverbConfig(
            environmentalReverb!!.roomLevel,
            environmentalReverb!!.roomHFLevel,
            environmentalReverb!!.decayTime,
            environmentalReverb!!.decayHFRatio,
            environmentalReverb!!.reflectionsLevel,
            environmentalReverb!!.reflectionsDelay,
            environmentalReverb!!.reverbLevel,
            environmentalReverb!!.reverbDelay,
            environmentalReverb!!.diffusion,
            environmentalReverb!!.density
        )
        set(value) {
            if (value == null) {
                reverbEnabled = false
                return
            }

            environmentalReverb?.roomLevel = value.roomLevel
            environmentalReverb?.roomHFLevel = value.roomHFLevel
            environmentalReverb?.decayTime = value.decayTime
            environmentalReverb?.decayHFRatio = value.decayHFRatio
            environmentalReverb?.reflectionsLevel = value.reflectionsLevel
            environmentalReverb?.reflectionsDelay = value.reflectionsDelay
            environmentalReverb?.reverbLevel = value.reverbLevel
            environmentalReverb?.reverbDelay = value.reverbDelay
            environmentalReverb?.diffusion = value.diffusion
            environmentalReverb?.density = value.density
        }


    override fun setBandLevel(band: Int, level: SoundLevel) {
        assert(level in minBandLevel..maxBandLevel) { "BandLevel $level is invalid (range: $minBandLevel..$maxBandLevel)" }
        assert(band in 0..numBands) { "Band $band is invalid (max: ${numBands - 1})" }
        equalizer?.setBandLevel(band.toShort(), level.inmDb.toShort())
    }

    override fun setPreset(preset: String) {
        assert(preset in presets) { "Preset $preset not found in available presets" }
        equalizer?.usePreset(presets.indexOf(preset).toShort())
    }

    override fun getBandLevel(band: Int): SoundLevel {
        assert(band in 0..numBands) { "Band $band is invalid (max: ${numBands - 1})" }
        return equalizer?.getBandLevel(band.toShort())?.mDb ?: 0.mDb
    }

    override fun getBandIndex(
        lowerBandFrequency: SoundFrequency,
        upperBandFrequency: SoundFrequency,
        centerFrequency: SoundFrequency
    ): Int? {
        val idx = bands?.indexOfFirst { it ->
            it.lowerBandFrequency == lowerBandFrequency && it.upperBandFrequency == upperBandFrequency && it.centerFrequency == centerFrequency
        }
        if (idx == null || idx == -1)
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
        volumeEnhancer = LoudnessEnhancer(audioSessionId)
    }

    override fun release() {
        equalizer?.release()
        virtualizer?.release()
        bassBoost?.release()
        environmentalReverb?.release()
        volumeEnhancer?.release()
    }
}