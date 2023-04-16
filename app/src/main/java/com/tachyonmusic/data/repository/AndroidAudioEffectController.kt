package com.tachyonmusic.data.repository

import android.media.audiofx.BassBoost
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

    override var bass: Int
        get() = bassBoost?.roundedStrength?.toInt() ?: 0
        set(value) {
            bassBoost?.setStrength(value.toShort())
        }

    override var virtualizerStrength: Int
        get() = virtualizer?.roundedStrength?.toInt() ?: 0
        set(value) {
            virtualizer?.setStrength(value.toShort())
        }

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

    override val numBands: Int
        get() = equalizer?.numberOfBands?.toInt() ?: 0

    override val minBandLevel: Int
        get() = equalizer?.bandLevelRange?.first()?.toInt() ?: 0

    override val maxBandLevel: Int
        get() = equalizer?.bandLevelRange?.last()?.toInt() ?: 0


    override fun setBandLevel(band: Int, level: Int) {
        assert(band in minBandLevel..maxBandLevel) { "Band $band is invalid (range: $minBandLevel..$maxBandLevel)" }
        equalizer?.setBandLevel(band.toShort(), level.toShort())
    }

    override fun getBandLevel(band: Int): Int {
        assert(band in 0..numBands) { "Band $band is invalid (range: $minBandLevel..$maxBandLevel)" }
        return equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0
    }

    // TODO: Reverb, DynamicProcessing, HapticGenerator

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
    }
}