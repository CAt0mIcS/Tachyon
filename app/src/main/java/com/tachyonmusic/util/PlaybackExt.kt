package com.tachyonmusic.util

import com.tachyonmusic.core.domain.playback.Playback

val Playback.displayTitle: String
    get() = if (isSong) title else if (isRemix) name!! else TODO("Invalid playback type")

val Playback.displaySubtitle: String
    get() = if (isSong)
        artist
    else if (isRemix) {
        if (isPlayable) "$title by $artist" else "Missing: ${mediaId.uri}"
    } else TODO("Invalid playback type")