package com.daton.database.domain.model

import com.tachyonmusic.core.domain.MediaId

open class SinglePlaybackEntity(
    var mediaId: MediaId,
    var title: String,
    var artist: String,
    var duration: Long,

    /**
     * Specifies a url to download the artwork from if not null. If it's an empty string we
     * already tried to load the artwork but didn't find any matches (currently checks every time and
     * doesn't set it to an empty string if we find nothing)
     */
    var artwork: String? = null,
) : PlaybackEntity()