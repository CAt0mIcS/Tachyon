package com.tachyonmusic.presentation.home.model

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.util.displaySubtitle
import com.tachyonmusic.util.displayTitle

data class HomeEntity(
    val displayTitle: String,
    val displaySubtitle: String,
    val mediaId: MediaId,
    val isPlayable: Boolean,
    val artwork: Artwork? = null
)

fun Playback.toHomeEntity() = HomeEntity(
    displayTitle,
    displaySubtitle,
    mediaId,
    isPlayable,
    artwork
)