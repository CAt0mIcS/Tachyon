package com.tachyonmusic.playback_layers

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.ms

fun SongEntity.toPlayback(artwork: Artwork?, isPlayable: Boolean) =
    Playback(
        mediaId,
        title,
        artist,
        duration,
        isPlayable,
        album,
        artwork,
        isArtworkLoading = artworkType == ArtworkType.UNKNOWN,
        isHidden = isHidden,
        timestampCreatedAddedEdited = timestampCreatedAddedEdited
    )

fun RemixEntity.toPlayback(song: Playback?) =
    Playback(
        mediaId,
        songTitle,
        songArtist,
        songDuration,
        isPlayable = song != null,
        song?.album,
        song?.artwork,
        song?.isArtworkLoading ?: false,
        TimingDataController(timingData ?: listOf(TimingData(0.ms, songDuration))),
        isHidden = false,
        bassBoost ?: 0,
        virtualizerStrength ?: 0,
        equalizerBands,
        playbackParameters ?: PlaybackParameters(),
        reverb,
        timestampCreatedAddedEdited
    )
