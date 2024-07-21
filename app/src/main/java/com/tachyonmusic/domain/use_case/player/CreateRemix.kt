package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.media.domain.AudioEffectController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.contract

class CreateRemix(
    private val audioEffectController: AudioEffectController
) {
    suspend operator fun invoke(
        name: String,
        playback: SinglePlayback?,
        timingData: TimingDataController?
    ) = withContext(Dispatchers.IO) {
        if (!isValidPlayback(playback, timingData)) {
            Resource.Error(
                UiText.StringResource(
                    R.string.cannot_create_remix,
                    name,
                    playback.toString(),
                    playback?.timingData.toString()
                )
            )
        } else {
            val song = playback.underlyingSong
            val remix = runOnUiThread {
                RemixEntity(
                    MediaId.ofLocalRemix(name, song.mediaId),
                    song.title,
                    song.artist,
                    song.duration,
                    playback.timingData?.timingData,
                    currentTimingDataIndex = 0,
                    audioEffectController.bassValue,
                    audioEffectController.virtualizerValue,
                    audioEffectController.equalizerBandValues,
                    audioEffectController.playbackParams.value,
                    audioEffectController.reverbValue
                )
            }

            Resource.Success(remix)
        }
    }

    private fun isValidPlayback(
        playback: SinglePlayback?,
        timingData: TimingDataController?
    ): Boolean {
        contract {
            returns(true) implies (playback != null)
        }

        return playback != null // TODO: Check if timing data is valid or if audio effects are applied
    }
}