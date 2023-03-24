package com.tachyonmusic.media.util

import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.media.R
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText


// TODO: Should only be one function in CustomPlayer?
fun CustomPlayer.updateTimingDataOfCurrentPlayback(timingData: TimingDataController?): Resource<Unit> {
    if (currentMediaItem?.mediaMetadata?.timingData == null)
        return Resource.Error(UiText.StringResource(R.string.invalid_playback))
    if (timingData == null)
        return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

    updateTimingData(timingData)
    return Resource.Success()
}