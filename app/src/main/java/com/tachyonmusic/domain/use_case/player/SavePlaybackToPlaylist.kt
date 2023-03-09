package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.PlaybackRepository
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavePlaybackToPlaylist(
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(playback: SinglePlayback?, i: Int) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val playlist = playbackRepository.getPlaylists().getOrNull(i)
        if (playlist == null || playlist.hasPlayback(playback))
            return@withContext

        playlist.add(playback)

        playlistRepository.setPlaybacksOfPlaylist(
            playlist.mediaId,
            playlist.playbacks.map { it.mediaId })
    }
}