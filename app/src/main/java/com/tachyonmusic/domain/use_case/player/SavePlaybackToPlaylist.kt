package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavePlaybackToPlaylist(
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(playback: Playback?, playlistIndex: Int) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext

        val playlist = playbackRepository.getPlaylists().getOrNull(playlistIndex)
        if (playlist == null || playlist.hasPlayback(playback))
            return@withContext

        val newPlaylist = playlist.copy(
            playbacks = playlist.playbacks.toMutableList().apply { add(playback) })
        playlistRepository.setPlaybacksOfPlaylist(
            newPlaylist.mediaId,
            newPlaylist.playbacks.map { it.mediaId }
        )

        runOnUiThread {
            if (browser.currentPlaylist.value == playlist)
                browser.setPlaylist(newPlaylist) // TODO: Check if we need to re-prepare, seek to correct item, etc...
        }
    }
}