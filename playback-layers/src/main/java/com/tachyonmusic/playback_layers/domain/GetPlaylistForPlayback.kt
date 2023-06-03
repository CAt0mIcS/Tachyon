package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.core.data.playback.LocalPlaylistImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
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

    suspend operator fun invoke(playback: SinglePlayback?) = invoke(playback?.mediaId)

    suspend operator fun invoke(mediaId: MediaId?) = withContext(Dispatchers.IO) {
        if (mediaId == null)
            return@withContext null

        if (mediaId.isLocalSong)
            getSongPlaylist(mediaId)
        else if (mediaId.isLocalCustomizedSong)
            getCustomizedSongPlaylist(mediaId)
        else null
    }


    private suspend fun getSongPlaylist(
        mediaId: MediaId
    ): Playlist {
        val items = predefinedPlaylistsRepository.songPlaylist.value
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylistImpl.build(
            predefinedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == mediaId }
        )
    }


    private suspend fun getCustomizedSongPlaylist(
        mediaId: MediaId
    ): Playlist {
        val items = predefinedPlaylistsRepository.customizedSongPlaylist.value
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylistImpl.build(
            predefinedCustomizedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOfFirst { it.mediaId == mediaId }
        )
    }
}