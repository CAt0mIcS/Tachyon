package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemovePlaybackFromPlaylist(
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository,
    private val browser: MediaBrowserController
) {
    suspend operator fun invoke(toRemove: SinglePlayback?, playlist: Playlist?) =
        withContext(Dispatchers.IO) {
            if (toRemove == null || playlist == null || !playlist.hasPlayback(toRemove))
                return@withContext

            val copy = playlist.copy()
            copy.remove(toRemove)
            playlistRepository.setPlaybacksOfPlaylist(
                copy.mediaId,
                copy.playbacks.map { it.mediaId })

            runOnUiThread {
                if(browser.currentPlaylist.value != null)
                    browser.setMediaItems(copy) // TODO: Check if we need to re-prepare, seek to correct item, etc...
            }
        }

    suspend operator fun invoke(toRemove: SinglePlayback?, i: Int) = withContext(Dispatchers.IO) {
        invoke(toRemove, playbackRepository.getPlaylists().getOrNull(i))
    }
}