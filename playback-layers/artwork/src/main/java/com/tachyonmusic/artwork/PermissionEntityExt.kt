package com.tachyonmusic.artwork

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
    name,
    TimingDataController(timingData, currentTimingDataIndex),
    song
)

fun PlaylistPermissionEntity.toPlaylist(items: List<SinglePlayback>): Playlist =
    RemotePlaylistImpl.build(mediaId, items.toMutableList(), currentItemIndex)


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