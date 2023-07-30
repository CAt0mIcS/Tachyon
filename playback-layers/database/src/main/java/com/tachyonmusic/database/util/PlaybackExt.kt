package com.tachyonmusic.database.util

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity

fun Playback.toEntity(): PlaybackEntity = when (this) {
    is Song -> toEntity()
    is CustomizedSong -> toEntity()
    is Playlist -> toEntity()
    else -> TODO("Invalid playback type ${this::class.java.name}")
}

fun SinglePlayback.toEntity(): SinglePlaybackEntity = when (this) {
    is Song -> toEntity()
    is CustomizedSong -> toEntity()
    else -> TODO("Invalid SinglePlayback type ${this::class.java.name}")
}

fun Song.toEntity(): SongEntity {
    val artworkType = ArtworkType.getType(this)
    return SongEntity(
        mediaId,
        title,
        artist,
        duration,
        isHidden,
        isPlayable,
        artworkType,
        if (artwork is RemoteArtwork)
            (artwork as RemoteArtwork).uri.toURL().toString()
        else null
    )
}

fun CustomizedSong.toEntity(): CustomizedSongEntity {
    return CustomizedSongEntity(
        mediaId,
        title,
        artist,
        duration,
        timingData?.timingData,
        timingData?.currentIndex ?: 0,
        bassBoost, virtualizerStrength, equalizerBands, playbackParameters, reverb
    )
}

fun Playlist.toEntity() = PlaylistEntity(
    name,
    mediaId,
    playbacks.map { it.mediaId },
    currentPlaylistIndex
)
