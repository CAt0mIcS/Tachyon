package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.playback_layers.predefinedCustomizedSongPlaylistMediaId
import com.tachyonmusic.playback_layers.predefinedSongPlaylistMediaId
import com.tachyonmusic.util.indexOf
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

        if (playback.mediaId.isLocalSong)
            getSongPlaylist(playback.mediaId)
        else if (playback.mediaId.isSpotify)
            LocalPlaylist(
                MediaId.ofLocalPlaylist(playback.mediaId.toString()),
                mutableListOf(playback),
                0
            )
        else if (playback.mediaId.isLocalCustomizedSong)
            getCustomizedSongPlaylist(playback.mediaId)
        else null
    }


    private suspend fun getSongPlaylist(
        mediaId: MediaId
    ): Playlist {
        val items = predefinedPlaylistsRepository.songPlaylist.value
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylist.build(
            predefinedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOf { it.mediaId == mediaId } ?: 0
        )
    }


    private suspend fun getCustomizedSongPlaylist(
        mediaId: MediaId
    ): Playlist {
        val items = predefinedPlaylistsRepository.customizedSongPlaylist.value
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylist.build(
            predefinedCustomizedSongPlaylistMediaId,
            items.toMutableList(),
            items.indexOf { it.mediaId == mediaId } ?: 0
        )
    }
}