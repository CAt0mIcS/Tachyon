package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import com.tachyonmusic.media.R
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class PreparePlayer(
    private val player: CustomPlayer
) {
    operator fun invoke(items: List<MediaItem>?, initialWindowIndex: Int?): Resource<Unit> {
        if (items == null || initialWindowIndex == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_arguments))

        player.setMediaItems(items)
        player.seekTo(initialWindowIndex, C.TIME_UNSET)
        player.prepare()
        return Resource.Success()
    }
}