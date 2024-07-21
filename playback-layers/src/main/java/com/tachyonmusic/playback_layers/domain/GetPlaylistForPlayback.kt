package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.playback_layers.predefinedRemixPlaylistMediaId
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

    suspend operator fun invoke(playback: SinglePlayback?) = invoke(playback?.mediaId)

    suspend operator fun invoke(mediaId: MediaId?) = withContext(Dispatchers.IO) {
        if (mediaId == null)
            return@withContext null

        if (mediaId.isLocalSong)
            getSongPlaylist(mediaId)
        else if (mediaId.isLocalRemix)
            getRemixPlaylist(mediaId)
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
            items.indexOf { it.mediaId == mediaId } ?: 0,
            timestampCreatedAddedEdited = 0L
        )
    }


    private suspend fun getRemixPlaylist(
        mediaId: MediaId
    ): Playlist {
        val items = predefinedPlaylistsRepository.remixPlaylist.value
        items.forEach {
            artworkCodex.await(it.mediaId.underlyingMediaId ?: it.mediaId)
        }

        return LocalPlaylist.build(
            predefinedRemixPlaylistMediaId,
            items.toMutableList(),
            items.indexOf { it.mediaId == mediaId } ?: 0,
            timestampCreatedAddedEdited = 0L
        )
    }
}