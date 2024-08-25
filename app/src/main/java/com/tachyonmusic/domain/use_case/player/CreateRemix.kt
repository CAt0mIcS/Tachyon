package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.media.domain.AudioEffectController
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.contract

class CreateRemix
{
    suspend operator fun invoke(
        name: String,
        playback: Playback?
    ) = withContext(Dispatchers.IO) {
        if (!isValidPlayback(playback)) {
            Resource.Error(
                UiText.StringResource(
                    R.string.cannot_create_remix,
                    name,
                    playback.toString(),
                    playback?.timingData.toString()
                )
            )
        } else {
            val remix = RemixEntity(
                MediaId.ofLocalRemix(
                    name,
                    if (playback.isRemix) playback.mediaId.underlyingMediaId!! else playback.mediaId
                ),
                playback.title,
                playback.artist,
                playback.duration,
                playback.timingData.timingData,
                currentTimingDataIndex = 0,
                playback.bassBoost,
                playback.virtualizerStrength,
                playback.equalizerBands,
                playback.playbackParameters,
                playback.reverb
            )

            Resource.Success(remix)
        }
    }

    private fun isValidPlayback(
        playback: Playback?
    ): Boolean {
        contract {
            returns(true) implies (playback != null)
        }

        return playback != null // TODO: Check if timing data is valid or if audio effects are applied
    }
}