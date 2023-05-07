package com.tachyonmusic.domain.use_case

import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.core.data.playback.LocalPlaylistImpl
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.PredefinedPlaylistsRepository
import com.tachyonmusic.predefinedCustomizedSongPlaylistMediaId
import com.tachyonmusic.predefinedSongPlaylistMediaId
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
            is CustomizedSong -> getCustomizedSongPlaylist(playback)
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

        return LocalPlaylistImpl.build(
            predefinedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == playback.mediaId }
        )
    }


    private suspend fun getCustomizedSongPlaylist(
        playback: SinglePlayback
    ): Playlist {
        val items = predefinedPlaylistsRepository.customizedSongPlaylist
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylistImpl.build(
            predefinedCustomizedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == playback.mediaId }
        )
    }
}