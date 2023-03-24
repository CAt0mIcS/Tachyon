package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId

@Entity
class PlaylistEntity(
    mediaId: MediaId,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,
) : PlaybackEntity(mediaId) {

    val name: String
        get() = mediaId.source.replace(PlaybackType.Playlist.Remote().toString(), "")
}