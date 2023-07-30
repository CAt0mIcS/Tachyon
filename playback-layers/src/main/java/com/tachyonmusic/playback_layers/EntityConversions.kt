package com.tachyonmusic.playback_layers

import android.net.Uri
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalCustomizedSong
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.util.ms
import java.net.URI

fun SongEntity.toLocalSong(artwork: Artwork?, isPlayable: Boolean) =
    LocalSong(
        mediaId.uri!!,
        mediaId,
        title,
        artist,
        duration,
        isHidden
    ).let {
        it.isPlayable = isPlayable
        it.isArtworkLoading = artworkType == ArtworkType.UNKNOWN
        it.artwork = artwork
        it
    }

fun SongEntity.toSpotifySong() =
    SpotifySong(
        mediaId,
        title,
        artist,
        duration,
        isHidden
    ).let {
        it.isPlayable = isPlayable
        it.artwork = RemoteArtwork(URI(artworkUrl))
        it
    }

fun CustomizedSongEntity.toCustomizedSong(song: Song?) =
    LocalCustomizedSong(
        mediaId,
        song ?: LocalSong(
            Uri.EMPTY,
            MediaId.EMPTY,
            "",
            "",
            0.ms,
            true
        ) // TODO: Better way of displaying loops of deleted songs
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
