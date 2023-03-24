package com.tachyonmusic.domain.use_case

import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.PredefinedPlaylistsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OPTIMIZE: Only load artwork and isPlayable state if required
 */
class GetPlaylistForPlayback(
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val artworkCodex: ArtworkCodex
) {

    suspend operator fun invoke(playback: SinglePlayback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext null

        when (playback) {
            is Song -> getSongPlaylist(playback)
            is Loop -> getLoopPlaylist(playback)
            else -> null
        }
    }


    private suspend fun getSongPlaylist(
        playback: SinglePlayback
    ): Playlist {
        val items = predefinedPlaylistsRepository.songPlaylist
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return RemotePlaylistImpl.build(
            predefinedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == playback.mediaId }
        )
    }


    private suspend fun getLoopPlaylist(
        playback: SinglePlayback
    ): Playlist {
        val items = predefinedPlaylistsRepository.loopPlaylist
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return RemotePlaylistImpl.build(
            predefinedLoopPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == playback.mediaId }
        )
    }
}

private val predefinedLoopPlaylistMediaId =
    MediaId.ofRemotePlaylist("com.tachyonmusic.PREDEFINED_LOOPS_PLAYLIST")

private val predefinedSongPlaylistMediaId =
    MediaId.ofRemotePlaylist("com.tachyonmusic.PREDEFINED_SONGS_PLAYLIST")

val Playlist.isPredefined: Boolean
    get() = mediaId == predefinedSongPlaylistMediaId || mediaId == predefinedLoopPlaylistMediaId