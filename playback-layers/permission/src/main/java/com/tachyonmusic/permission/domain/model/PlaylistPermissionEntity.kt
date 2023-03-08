package com.tachyonmusic.permission.domain.model

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity

data class PlaylistPermissionEntity(
    val mediaId: MediaId,
    val items: List<SinglePlaybackPermissionEntity>,
    val currentItemIndex: Int
)

internal fun PlaylistEntity.toPermissionEntity(items: List<SinglePlaybackPermissionEntity>) =
    PlaylistPermissionEntity(mediaId, items, currentItemIndex)