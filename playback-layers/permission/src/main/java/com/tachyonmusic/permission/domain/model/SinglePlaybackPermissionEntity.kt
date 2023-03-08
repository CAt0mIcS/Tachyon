package com.tachyonmusic.permission.domain.model

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.Duration

open class SinglePlaybackPermissionEntity(
    val mediaId: MediaId,
    val title: String,
    val artist: String,
    val duration: Duration,
    val isPlayable: Boolean
)