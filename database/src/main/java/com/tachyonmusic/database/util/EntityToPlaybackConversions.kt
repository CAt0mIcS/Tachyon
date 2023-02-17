package com.tachyonmusic.database.util

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.util.ms

fun SongEntity.toSong() = LocalSongImpl(mediaId, title, artist, duration)

fun LoopEntity.toLoop() =
    RemoteLoopImpl(
        mediaId,
        mediaId.source.replace(PlaybackType.Loop.Remote().toString(), ""),
        TimingDataController(timingData, currentTimingDataIndex),
        LocalSongImpl(
            mediaId.underlyingMediaId!!,
            title,
            artist,
            duration
        )
    )

suspend fun PlaylistEntity.toPlaylist(
    songRepository: SongRepository,
    loopRepository: LoopRepository
): Playlist {
    return RemotePlaylistImpl.build(
        mediaId,
        items.map {
            return@map songRepository.findByMediaId(it)?.toSong()
                ?: loopRepository.findByMediaId(it)?.toLoop()
        }.filterNotNull()
            .toMutableList(), // TODO: Warn user that some playback is missing in playlist
        currentItemIndex
    )
}

suspend fun PlaybackEntity.toPlayback(
    songRepository: SongRepository,
    loopRepository: LoopRepository
) = when (this) {
    is SongEntity -> toSong()
    is LoopEntity -> toLoop()
    is PlaylistEntity -> toPlaylist(songRepository, loopRepository)
    else -> TODO("Invalid playback type ${this::class.java.name}")
}
