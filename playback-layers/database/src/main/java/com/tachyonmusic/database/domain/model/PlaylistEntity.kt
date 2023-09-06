package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId

const val PLAYLIST_DATABASE_TABLE_NAME = "Playlists"

@Entity(tableName = PLAYLIST_DATABASE_TABLE_NAME)
class PlaylistEntity(
    val name: String,
    mediaId: MediaId,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,
) : PlaybackEntity(mediaId)