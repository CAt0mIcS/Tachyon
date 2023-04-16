package com.tachyonmusic.media.data

import android.media.audiofx.*
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.media.domain.AudioEffectController

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

    override val minBandLevel: Int
        get() = if (!equalizerEnabled) 0 else equalizer?.bandLevelRange?.first()?.toInt() ?: 0

    override val maxBandLevel: Int
        get() = if (!equalizerEnabled) 0 else equalizer?.bandLevelRange?.last()?.toInt() ?: 0

    override val bands: List<Int>?
        get() = if (!equalizerEnabled) null else List(numBands) {
            getBandLevel(it)
        }

    /**************************************************************************
     ********** Reverb | TODO: Choose appropriate default values for null case
     *************************************************************************/
    override var reverb: ReverbConfig?
        get() = if (!reverbEnabled) null else ReverbConfig(
            environmentalReverb?.roomLevel?.toInt() ?: 0,
            environmentalReverb?.roomHFLevel?.toInt() ?: 0,
            environmentalReverb?.decayTime ?: 100,
            environmentalReverb?.decayHFRatio?.toInt() ?: 1000,
            environmentalReverb?.reflectionsLevel?.toInt() ?: 0,
            environmentalReverb?.reflectionsDelay ?: 0,
            environmentalReverb?.reverbLevel?.toInt() ?: 0,
            environmentalReverb?.reverbDelay ?: 0,
            environmentalReverb?.diffusion?.toInt() ?: 0,
            environmentalReverb?.density?.toInt() ?: 0
        )
        set(value) {
            if (value == null) {
                reverbEnabled = false
                return
            }

            environmentalReverb?.roomLevel = value.roomLevel.toShort()
            environmentalReverb?.roomHFLevel = value.roomHFLevel.toShort()
            environmentalReverb?.decayTime = value.decayTime
            environmentalReverb?.decayHFRatio = value.decayHFRatio.toShort()
            environmentalReverb?.reflectionsLevel = value.reflectionsLevel.toShort()
            environmentalReverb?.reflectionsDelay = value.reflectionsDelay
            environmentalReverb?.reverbLevel = value.reverbLevel.toShort()
            environmentalReverb?.reverbDelay = value.reverbDelay
            environmentalReverb?.diffusion = value.diffusion.toShort()
            environmentalReverb?.density = value.density.toShort()
        }


    override fun setBandLevel(band: Int, level: Int) {
        assert(band in minBandLevel..maxBandLevel) { "Band $band is invalid (range: $minBandLevel..$maxBandLevel)" }
        equalizer?.setBandLevel(band.toShort(), level.toShort())
    }

    override fun getBandLevel(band: Int): Int {
        assert(band in 0..numBands) { "Band $band is invalid (range: $minBandLevel..$maxBandLevel)" }
        return equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0
    }

    // TODO: DynamicProcessing, HapticGenerator
    // TODO: Release audio effects

    override fun updateAudioSessionId(audioSessionId: Int) {
        equalizer = Equalizer(Int.MAX_VALUE, audioSessionId)
        virtualizer = Virtualizer(Int.MAX_VALUE, audioSessionId)
        bassBoost = BassBoost(Int.MAX_VALUE, audioSessionId)
        environmentalReverb = EnvironmentalReverb(Int.MAX_VALUE, audioSessionId)
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