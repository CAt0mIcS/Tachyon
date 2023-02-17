package com.tachyonmusic.media.util

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.media.R
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

fun CustomPlayer.prepare(
    items: List<MediaItem>?,
    initialWindowIndex: Int?
): Resource<Unit> {
    if (items == null || initialWindowIndex == null)
        return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

    setMediaItems(items)
    seekTo(initialWindowIndex, C.TIME_UNSET)
    prepare()
    return Resource.Success()
}


// TODO: Should only be one function in CustomPlayer?
fun CustomPlayer.updateTimingDataOfCurrentPlayback(timingData: TimingDataController?): Resource<Unit> {
    if (currentMediaItem?.mediaMetadata?.timingData == null)
        return Resource.Error(UiText.StringResource(R.string.invalid_playback))
    if (timingData == null)
        return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

    updateTimingData(timingData)
    return Resource.Success()
}