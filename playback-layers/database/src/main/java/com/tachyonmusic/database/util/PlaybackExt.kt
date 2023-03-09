package com.tachyonmusic.database.util

import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.database.domain.model.*

fun Playback.toEntity(): PlaybackEntity = when (this) {
    is Song -> toEntity()
    is Loop -> toEntity()
    is Playlist -> toEntity()
    else -> TODO("Invalid playback type ${this::class.java.name}")
}

fun SinglePlayback.toEntity(): SinglePlaybackEntity = when (this) {
    is Song -> toEntity()
    is Loop -> toEntity()
    else -> TODO("Invalid SinglePlayback type ${this::class.java.name}")
}

fun Song.toEntity(): SongEntity {
    val artworkType = ArtworkType.getType(this)
    return SongEntity(
        mediaId,
        title,
        artist,
        duration,
        artworkType,
        if (artwork.value is RemoteArtwork)
            (artwork.value as RemoteArtwork).uri.toURL().toString()
        else null
    )
}

fun Loop.toEntity(): LoopEntity {
    return LoopEntity(
        mediaId,
        title,
        artist,
        duration,
        timingData?.timingData ?: emptyList(),
        timingData?.currentIndex ?: 0,
    )
}

fun Playlist.toEntity() = PlaylistEntity(
    mediaId,
    playbacks.map { it.mediaId },
    currentPlaylistIndex
)
