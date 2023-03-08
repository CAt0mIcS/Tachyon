package com.tachyonmusic.artwork

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.PlaylistPermissionEntity
import com.tachyonmusic.permission.domain.model.SinglePlaybackPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import kotlinx.coroutines.flow.update

fun SongPermissionEntity.toSong(): Song =
    LocalSongImpl(mediaId.uri!!, mediaId, title, artist, duration).let {
        it.isPlayable.value = isPlayable
        it
    }

fun LoopPermissionEntity.toLoop(
    song: Song = LocalSongImpl(
        mediaId.uri!!,
        mediaId,
        title,
        artist,
        duration
    ).let {
        it.isPlayable.update { isPlayable }
        it
    }
): Loop = RemoteLoopImpl(
    mediaId,
    mediaId.source.replace(PlaybackType.Loop.Remote().toString(), ""),
    TimingDataController(timingData, currentTimingDataIndex),
    song
)

fun PlaylistPermissionEntity.toPlaylist(): Playlist =
    RemotePlaylistImpl.build(mediaId, items.map {
        it.toSinglePlayback()
    }.toMutableList(), currentItemIndex)

fun SinglePlaybackPermissionEntity.toSinglePlayback() = when (this) {
    is SongPermissionEntity -> toSong()
    is LoopPermissionEntity -> toLoop()
    else -> TODO("Invalid permission entity ${this::class.java.name}")
}

fun Song.toPermissionEntity(artworkType: String, artworkUrl: String? = null) = SongPermissionEntity(
    mediaId,
    title,
    artist,
    duration,
    isPlayable.value,
    artworkType,
    artworkUrl
)


fun SongPermissionEntity.toSongEntity() =
    SongEntity(mediaId, title, artist, duration, artworkType, artworkUrl)