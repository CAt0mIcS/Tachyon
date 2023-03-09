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

fun PlaylistPermissionEntity.toPlaylist(songs: List<Song>, loops: List<Loop>): Playlist =
    RemotePlaylistImpl.build(mediaId, items.mapNotNull { singlePb ->
        singlePb.getFrom(songs, loops)
    }.toMutableList(), currentItemIndex)

fun SinglePlaybackPermissionEntity.getFrom(songs: List<Song>, loops: List<Loop>) =
    if (mediaId.isLocalSong)
        songs.find { it.mediaId == mediaId }
    else if (mediaId.isRemoteLoop)
        loops.find { it.mediaId == mediaId }
    else
        TODO("Invalid media id for single playback item $mediaId")


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