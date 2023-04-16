package com.tachyonmusic.data.repository

import android.media.audiofx.BassBoost
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.media3.common.PlaybackParameters
import com.tachyonmusic.domain.repository.AudioEffectController
import com.tachyonmusic.domain.repository.MediaBrowserController

class AndroidAudioEffectController(
    private val mediaBrowser: MediaBrowserController
) : AudioEffectController {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var reverb: EnvironmentalReverb? = null

    /**************************************************************************
     ********** Bass
     *************************************************************************/

    override var bass: Int
        get() = bassBoost?.roundedStrength?.toInt() ?: 0
        set(value) {
            bassBoost?.setStrength(value.toShort())
        }

    /**************************************************************************
     ********** Virtualizer
     *************************************************************************/

    override var virtualizerStrength: Int
        get() = virtualizer?.roundedStrength?.toInt() ?: 0
        set(value) {
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
        get() = reverb?.enabled == true && reverb?.hasControl() == true
        set(value) {
            reverb?.enabled = value
        }

    /**************************************************************************
     ********** Speed/Pitch
     *************************************************************************/

    override var speed: Float
        get() = mediaBrowser.playbackParameters.speed
        set(value) {
            mediaBrowser.playbackParameters = mediaBrowser.playbackParameters.withSpeed(value)
        }

    override var pitch: Float
        get() = mediaBrowser.playbackParameters.pitch
        set(value) {
            mediaBrowser.playbackParameters = PlaybackParameters(speed, value)
        }

    /**************************************************************************
     ********** Equalizer
     *************************************************************************/

    override val numBands: Int
        get() = equalizer?.numberOfBands?.toInt() ?: 0

    override val minBandLevel: Int
        get() = equalizer?.bandLevelRange?.first()?.toInt() ?: 0

    override val maxBandLevel: Int
        get() = equalizer?.bandLevelRange?.last()?.toInt() ?: 0


    /**************************************************************************
     ********** Reverb | TODO: Choose appropriate default values for null case
     *************************************************************************/
    override var roomLevel: Int
        get() = reverb?.roomLevel?.toInt() ?: 0
        set(value) {
            reverb?.roomLevel = value.toShort()
        }

    override var roomHFLevel: Int
        get() = reverb?.roomHFLevel?.toInt() ?: 0
        set(value) {
            reverb?.roomHFLevel = value.toShort()
        }

    override var decayTime: Int
        get() = reverb?.decayTime ?: 100
        set(value) {
            reverb?.decayTime = value
        }

    override var decayHFRatio: Int
        get() = reverb?.decayHFRatio?.toInt() ?: 1000
        set(value) {
            reverb?.decayHFRatio = value.toShort()
        }

    override var reflectionsLevel: Int
        get() = reverb?.reflectionsLevel?.toInt() ?: 0
        set(value) {
            reverb?.reflectionsLevel = value.toShort()
        }

    override var reflectionsDelay: Int
        get() = reverb?.reflectionsDelay ?: 0
        set(value) {
            reverb?.reflectionsDelay = value
        }

    override var reverbLevel: Int
        get() = reverb?.reverbLevel?.toInt() ?: 0
        set(value) {
            reverb?.reverbLevel = value.toShort()
        }

    override var reverbDelay: Int
        get() = reverb?.reverbDelay ?: 0
        set(value) {
            reverb?.reverbDelay = value
        }

    override var diffusion: Int
        get() = reverb?.diffusion?.toInt() ?: 0
        set(value) {
            reverb?.diffusion = value.toShort()
        }

    override var density: Int
        get() = reverb?.density?.toInt() ?: 0
        set(value) {
            reverb?.density = value.toShort()
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

    init {
        mediaBrowser.registerEventListener(this)
        if (mediaBrowser.audioSessionId != null)
            onAudioSessionIdChanged(mediaBrowser.audioSessionId!!)

        // TODO: Release audio effects
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        equalizer = Equalizer(Int.MAX_VALUE, audioSessionId)
        virtualizer = Virtualizer(Int.MAX_VALUE, audioSessionId)
        bassBoost = BassBoost(Int.MAX_VALUE, audioSessionId)
        reverb = EnvironmentalReverb(Int.MAX_VALUE, audioSessionId)
    }
}