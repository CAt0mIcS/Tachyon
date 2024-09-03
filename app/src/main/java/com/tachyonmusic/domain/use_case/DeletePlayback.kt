package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.player.RemovePlaybackFromPlaylist
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

class DeletePlayback(
    private val remixRepository: RemixRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val historyRepository: HistoryRepository,
    private val predefinedPlaylistRepository: PredefinedPlaylistsRepository,
    private val mediaBrowser: MediaBrowserController
) {
    @JvmName("invokePlayback")
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext
        assert(playback.isRemix)

        historyRepository.removeHierarchical(playback.mediaId)
        val playlists = playbackRepository.getPlaylists()
        for (i in playlists.indices) {
            playlists[i].apply {
                removePlaybackFromPlaylist(playback, this)
            }
        }

        remixRepository.remove(playback.mediaId)

        /**
         * Update media browser to make sure current playlist does not include deleted playback
         */
        predefinedPlaylistRepository.remixPlaylist.takeWhile { remixes ->
            if(remixes.find { it.mediaId == playback.mediaId } == null) {
                mediaBrowser.updatePredefinedPlaylist()
                false
            }
            else
                true
        }.collect()
    }

    @JvmName("invokePlaylist")
    suspend operator fun invoke(playlist: Playlist?) = withContext(Dispatchers.IO) {
        if (playlist == null)
            return@withContext

        playlistRepository.remove(playlist.mediaId)
    }
}