package com.tachyonmusic.database.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.util.Duration

const val CUSTOMIZED_SONG_DATABASE_TABLE_NAME = "CustomizedSongs"

@Entity(tableName = CUSTOMIZED_SONG_DATABASE_TABLE_NAME)
class CustomizedSongEntity(
    mediaId: MediaId,
    val songTitle: String,
    val songArtist: String,
    val songDuration: Duration,
    val timingData: List<TimingData>? = null,
    val currentTimingDataIndex: Int = 0,

    val bassBoost: Int? = null,
    val virtualizerStrength: Int? = null,
    val equalizerBands: List<EqualizerBand>? = null,

    @Embedded
    val playbackParameters: PlaybackParameters? = null,

    @Embedded
    val reverb: ReverbConfig? = null
) : SinglePlaybackEntity(mediaId, songTitle, songArtist, songDuration)
