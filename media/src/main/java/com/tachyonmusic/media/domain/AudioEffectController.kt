package com.tachyonmusic.media.domain

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.model.SoundFrequency
import com.tachyonmusic.core.domain.model.SoundLevel

interface AudioEffectController {
    var controller: PlaybackController?

    var bass: Int?
    var virtualizerStrength: Int?

    var bassEnabled: Boolean
    var virtualizerEnabled: Boolean
    var equalizerEnabled: Boolean
    var reverbEnabled: Boolean
    var volumeEnhancerEnabled: Boolean

    var playbackParams: PlaybackParameters

    val numBands: Int
    val maxBandLevel: SoundLevel
    val minBandLevel: SoundLevel
    val bands: List<EqualizerBand>?

    var reverb: ReverbConfig?

    fun setBandLevel(band: Int, level: SoundLevel)
    fun getBandLevel(band: Int): SoundLevel
    fun getBandIndex(
        lowerBandFrequency: SoundFrequency,
        upperBandFrequency: SoundFrequency,
        centerFrequency: SoundFrequency
    ): Int?

    fun updateAudioSessionId(audioSessionId: Int)
    fun release()

    interface PlaybackController {
        fun onNewPlaybackParameters(params: PlaybackParameters)
    }
}