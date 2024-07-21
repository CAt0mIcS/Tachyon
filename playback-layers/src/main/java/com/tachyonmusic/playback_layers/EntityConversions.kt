package com.tachyonmusic.playback_layers

import android.net.Uri
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.playback.LocalRemix
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.ms

fun SongEntity.toLocalSong(artwork: Artwork?, isPlayable: Boolean) =
    LocalSong(
        mediaId.uri!!,
        mediaId,
        title,
        artist,
        duration,
        isHidden,
        timestampCreatedAddedEdited
    ).let {
        it.isPlayable = isPlayable
        it.isArtworkLoading = artworkType == ArtworkType.UNKNOWN
        it.artwork = artwork
        it.album = album
        it
    }

fun RemixEntity.toRemix(song: Song?) =
    LocalRemix(
        mediaId,
        song ?: LocalSong(
            Uri.EMPTY,
            MediaId.EMPTY,
            "",
            "",
            0.ms,
            true,
            0L
        ), // TODO: Better way of displaying loops of deleted songs
        timestampCreatedAddedEdited
    ).let {
        it.timingData = timingData?.let {
            TimingDataController(
                it,
                currentTimingDataIndex
            )
        }
        it.bassBoost = bassBoost
        it.virtualizerStrength = virtualizerStrength
        it.equalizerBands = equalizerBands
        it.playbackParameters = playbackParameters
        it.reverb = reverb
        it.isPlayable = song != null
        it
    }
