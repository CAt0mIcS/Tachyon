package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.database.domain.ArtworkType

@Entity
class LoopEntity(
    mediaId: MediaId,
    val songTitle: String,
    val songArtist: String,
    val songDuration: Long,
    val timingData: List<TimingData>,
    val currentTimingDataIndex: Int = 0,
    artworkType: String = ArtworkType.UNKNOWN,
    artworkUrl: String? = null
) : SinglePlaybackEntity(mediaId, songTitle, songArtist, songDuration, artworkType, artworkUrl)
