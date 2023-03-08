package com.tachyonmusic.permission.domain.model

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.Duration

class SongPermissionEntity(
    mediaId: MediaId,
    title: String,
    artist: String,
    duration: Duration,
    isPlayable: Boolean,
    var artworkType: String,
    var artworkUrl: String?
) : SinglePlaybackPermissionEntity(mediaId, title, artist, duration, isPlayable)


internal fun SongEntity.toPermissionEntity(isPlayable: Boolean) =
    SongPermissionEntity(mediaId, title, artist, duration, isPlayable, artworkType, artworkUrl)
