package com.tachyonmusic.database.domain.model

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration

open class SinglePlaybackEntity(
    mediaId: MediaId,
    var title: String,
    var artist: String,
    var duration: Duration
) : PlaybackEntity(mediaId)