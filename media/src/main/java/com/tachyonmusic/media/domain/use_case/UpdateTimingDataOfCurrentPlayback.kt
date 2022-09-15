package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.media.R
import com.tachyonmusic.media.data.ext.timingData
import com.tachyonmusic.media.domain.CustomPlayer

class UpdateTimingDataOfCurrentPlayback(
    private val player: CustomPlayer
) {
    operator fun invoke(timingData: TimingDataController?): Resource<Unit> {
        if (player.currentMediaItem?.mediaMetadata?.timingData == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_playback))
        if (timingData == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        player.updateTimingData(timingData)
        return Resource.Success()
    }
}