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
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers

/**
 * TODO: Optimize Artwork loading in [getArtworkForPlayback], especially the [EmbeddedArtwork] part
 *   We can't put this on a separate thread because when the state is marshalled and sent to
 *   [MediaPlaybackServiceMediaBrowserController.onMediaItemTransition] and then e.g. [Playback.isArtworkLoading]
 *   is set to false the playback in [onMediaItemTransition] won't get this update since it only marshalled
 *   the value and not the state [Playback.isArtworkLoading] itself
 */

fun SongEntity.toSong() =
    LocalSongImpl(mediaId, title, artist, duration).apply {
        isArtworkLoading.value = true
        artwork.value = getArtworkForPlayback(this@toSong)
        isArtworkLoading.value = false
    }


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
        ).apply {
            isArtworkLoading.value = true
            artwork.value = getArtworkForPlayback(this@toLoop)
            isArtworkLoading.value = false
        }
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
