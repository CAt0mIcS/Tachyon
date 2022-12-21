package com.tachyonmusic.database.util

import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity

fun Playback.toEntity(): PlaybackEntity? = when (this) {
    is Song -> toEntity()
    is Loop -> toEntity()
    is Playlist -> toEntity()
    else -> null
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
    val artworkType = ArtworkType.getType(this)
    return LoopEntity(
        mediaId,
        title,
        artist,
        duration,
        timingData.timingData,
        timingData.currentIndex,
        artworkType,
        if (artwork.value is RemoteArtwork)
            (artwork.value as RemoteArtwork).uri.toURL().toString()
        else null
    )
}

fun Playlist.toEntity() = PlaylistEntity(
    mediaId,
    playbacks.map { it.mediaId },
    currentPlaylistIndex
)
