package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.util.Duration

@Entity
class LoopEntity(
    mediaId: MediaId,
    val songTitle: String,
    val songArtist: String,
    val songDuration: Duration,
    val timingData: List<TimingData>,
    val currentTimingDataIndex: Int = 0,
) : SinglePlaybackEntity(mediaId, songTitle, songArtist, songDuration)
