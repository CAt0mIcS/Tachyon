package com.daton.database.domain.model

import com.daton.database.domain.ArtworkType
import com.tachyonmusic.core.domain.MediaId

open class SinglePlaybackEntity(
    var mediaId: MediaId,
    var title: String,
    var artist: String,
    var duration: Long,

    var artworkType: String = ArtworkType.NO_ARTWORK,

    var artworkUrl: String? = null
) : PlaybackEntity()