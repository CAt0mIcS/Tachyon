package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemovePlaybackFromPlaylist(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playback: SinglePlayback?, i: Int) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val playlist = playlistRepository.getPlaylists().getOrNull(i)
        if (playlist == null || !playlist.hasPlayback(playback))
            return@withContext

        playlist.remove(playback)
        playlistRepository.setPlaybacksOfPlaylist(
            playlist.mediaId,
            playlist.playbacks.map { it.mediaId })
    }
}