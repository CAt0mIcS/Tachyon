package com.daton.database.domain.model

import com.tachyonmusic.core.domain.MediaId

open class SinglePlaybackEntity(
    var mediaId: MediaId,
    var title: String,
    var artist: String,
    var duration: Long,

    var artworkType: String,
    var artworkUrl: String?
) : PlaybackEntity()