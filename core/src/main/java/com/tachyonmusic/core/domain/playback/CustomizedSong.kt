package com.tachyonmusic.core.domain.playback

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.isNullOrEmpty

interface CustomizedSong : SinglePlayback {
    val name: String
    val song: Song

    val timingDataLoopingEnabled: Boolean
        get() = !timingData.isNullOrEmpty()
    val bassBoostEnabled: Boolean
        get() = bassBoost != null && bassBoost != 0
    val virtualizerEnabled: Boolean
        get() = virtualizerStrength != null && virtualizerStrength != 0
    val equalizerEnabled: Boolean
        get() = !equalizerBandLevels.isNullOrEmpty()
    val playbackParametersEnabled: Boolean
        get() = playbackParameters != null
    val reverbEnabled: Boolean
        get() = reverb != null

    var bassBoost: Int?
    var virtualizerStrength: Int?
    var equalizerBandLevels: List<Int>?
    var playbackParameters: PlaybackParameters?
    var reverb: ReverbConfig?

    override fun copy(): CustomizedSong
}
