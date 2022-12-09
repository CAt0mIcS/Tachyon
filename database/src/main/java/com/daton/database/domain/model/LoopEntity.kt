package com.daton.database.domain.model

import androidx.room.Entity
import com.daton.database.domain.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData

@Entity
class LoopEntity(
    mediaId: MediaId,
    val songTitle: String,
    val songArtist: String,
    val songDuration: Long,
    val timingData: List<TimingData>,
    val currentTimingDataIndex: Int = 0,
    artworkType: String = ArtworkType.NO_ARTWORK,
    artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, songTitle, songArtist, songDuration, artworkType, artworkUrl)
