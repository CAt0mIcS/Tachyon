package com.tachyonmusic.database.domain.model

import com.tachyonmusic.core.domain.MediaId

open class SinglePlaybackEntity(
    mediaId: MediaId,
    var title: String,
    var artist: String,
    var duration: Long
) : PlaybackEntity(mediaId)