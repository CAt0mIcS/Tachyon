package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.UiText
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.media.R
import com.tachyonmusic.media.data.ext.timingData
import com.tachyonmusic.media.domain.CustomPlayer

class AddTimingDataToCurrentPlayback(
    private val player: CustomPlayer
) {
    operator fun invoke(timingData: List<TimingData>): Resource<Unit> {
        if (player.currentMediaItem?.mediaMetadata?.timingData == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_playback))

        player.addTimingData(timingData.filter {
            player.currentMediaItem?.mediaMetadata?.timingData?.contains(
                it
            ) == false
        })
        return Resource.Success()
    }
}