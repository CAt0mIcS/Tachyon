package com.daton.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId

@Entity
data class SongEntity(
    val mediaId: MediaId,
    val title: String,
    val artist: String,
    val duration: Long,

    /**
     * Specifies a url to download the artwork from if not null.
     */
    val artwork: String? = null,

    /**
     * Used as a filename for downloaded album art, should be checked before [artwork] to use
     * the cached artwork instead of downloading it again.
     * Defined in [PlaybackEntity]
     */
//    @PrimaryKey val id: Int? = null
) : PlaybackEntity()