package com.tachyonmusic.media.domain

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.util.IListenable

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
    val minBandLevel: Int
    val maxBandLevel: Int
    val bands: List<Int>

    var reverb: ReverbConfig?

    fun setBandLevel(band: Int, level: Int)
    fun getBandLevel(band: Int): Int

    fun updateAudioSessionId(audioSessionId: Int)
    fun release()

    interface PlaybackController {
        fun onNewPlaybackParameters(params: PlaybackParameters)
    }
}