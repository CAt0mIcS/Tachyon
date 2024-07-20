package com.tachyonmusic.core.domain.playback

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.core.domain.model.EqualizerBand

interface Remix : SinglePlayback {
    val name: String
    val song: Song
    override val album: String?
        get() = song.album

    val timingDataLoopingEnabled: Boolean
        get() = !timingData.isNullOrEmpty()
    val bassBoostEnabled: Boolean
        get() = bassBoost != null && bassBoost != 0
    val virtualizerEnabled: Boolean
        get() = virtualizerStrength != null && virtualizerStrength != 0
    val equalizerEnabled: Boolean
        get() = !equalizerBands.isNullOrEmpty()
    val playbackParametersEnabled: Boolean
        get() = playbackParameters != null
    val reverbEnabled: Boolean
        get() = reverb != null

    var bassBoost: Int?
    var virtualizerStrength: Int?
    var equalizerBands: List<EqualizerBand>?
    var playbackParameters: PlaybackParameters?
    var reverb: ReverbConfig?

    override fun copy(): Remix
}
