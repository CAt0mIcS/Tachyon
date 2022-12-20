package com.daton.database.util

import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.PlaylistEntity
import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song

fun Playback.toEntity() = when (this) {
    is Song -> {
        val artworkType = ArtworkType.getType(this)
        SongEntity(
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
    is Loop -> {
        val artworkType = ArtworkType.getType(this)
        LoopEntity(
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
    is Playlist -> {
        PlaylistEntity(
            mediaId,
            playbacks.map { it.mediaId },
            currentPlaylistIndex
        )
    }
    else -> null
}
