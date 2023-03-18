package com.tachyonmusic.permission

import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import kotlinx.coroutines.flow.update

fun SongEntity.toSong(isPlayable: Boolean): Song =
    LocalSongImpl(mediaId.uri!!, mediaId, title, artist, duration).let {
        it.isPlayable.value = isPlayable
        it
    }

fun LoopEntity.toLoop(
    isPlayable: Boolean,
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

fun PlaylistEntity.toPlaylist(items: List<SinglePlayback>): Playlist =
    RemotePlaylistImpl.build(mediaId, items.toMutableList(), currentItemIndex)

