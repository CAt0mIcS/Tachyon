package com.tachyonmusic.presentation.player.model

import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.displaySubtitle
import com.tachyonmusic.util.displayTitle
import com.tachyonmusic.util.ms

data class PlayerEntity(
    val displayTitle: String,
    val displaySubtitle: String,
    val duration: Duration,
    val mediaId: MediaId,
    val isPlayable: Boolean,

    val artwork: Artwork? = null,

    val timingData: TimingDataController = TimingDataController.default(duration),
    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val equalizerBands: List<EqualizerBand>? = null,
    val playbackParameters: PlaybackParameters = PlaybackParameters(),
    val reverb: ReverbConfig? = null
) {
    val playbackType: PlaybackType
        get() = mediaId.playbackType
}

fun Playback.toPlayerEntity() = PlayerEntity(
    displayTitle,
    displaySubtitle,
    duration,
    mediaId,
    isPlayable,
    artwork, timingData, bassBoost, virtualizerStrength, equalizerBands, playbackParameters, reverb
)