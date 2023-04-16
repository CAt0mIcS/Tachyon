package com.tachyonmusic.permission

import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.LocalCustomizedSongImpl
import com.tachyonmusic.core.data.playback.LocalPlaylistImpl
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity

fun SongEntity.toSong(isPlayable: Boolean): Song =
    LocalSongImpl(mediaId.uri!!, mediaId, title, artist, duration).let {
        it.isPlayable = isPlayable
        it
    }

fun CustomizedSongEntity.toCustomizedSong(
    isPlayable: Boolean,
    song: Song = LocalSongImpl(
        mediaId.underlyingMediaId!!.uri!!,
        mediaId.underlyingMediaId!!,
        title,
        artist,
        duration
    ).let {
        it.isPlayable = isPlayable
        it
    }
): CustomizedSong = LocalCustomizedSongImpl(
    mediaId,
    TimingDataController(timingData, currentTimingDataIndex),
    song
)

fun SinglePlaybackEntity.toPlayback(isPlayable: Boolean): SinglePlayback = when (this) {
    is SongEntity -> toSong(isPlayable)
    is CustomizedSongEntity -> toCustomizedSong(isPlayable)
    else -> TODO("Invalid SinglePlayback type ${this::class.java.name}")
}

fun PlaylistEntity.toPlaylist(items: List<SinglePlayback>): Playlist =
    LocalPlaylistImpl.build(mediaId, items.toMutableList(), currentItemIndex)

