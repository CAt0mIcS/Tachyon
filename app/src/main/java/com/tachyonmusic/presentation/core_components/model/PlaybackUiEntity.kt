package com.tachyonmusic.presentation.core_components.model

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId

data class PlaybackUiEntity(
    val displayTitle: String,
    val displaySubtitle: String,
    val mediaId: MediaId,
    val playbackType: PlaybackType,
    val artwork: Artwork?
)