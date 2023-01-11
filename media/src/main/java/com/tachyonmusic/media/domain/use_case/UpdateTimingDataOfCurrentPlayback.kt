package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.media.R
import com.tachyonmusic.media.data.ext.timingData
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class UpdateTimingDataOfCurrentPlayback {
    operator fun invoke(player: CustomPlayer, timingData: TimingDataController?): Resource<Unit> {
        if (player.currentMediaItem?.mediaMetadata?.timingData == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_playback))
        if (timingData == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        player.updateTimingData(timingData)
        return Resource.Success()
    }
}