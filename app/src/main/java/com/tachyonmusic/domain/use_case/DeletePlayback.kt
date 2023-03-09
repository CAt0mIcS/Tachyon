package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.playback_layers.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeletePlayback(
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        when (playback) {
            is Loop -> {
                loopRepository.remove(playback.mediaId)

                val playlists = playbackRepository.getPlaylists()
                for (i in playlists.indices) {
                    playlists[i].apply {
                        if (hasPlayback(playback)) {
                            remove(playback)
                            playlistRepository.setPlaybacksOfPlaylist(
                                mediaId,
                                playbacks.map { it.mediaId })
                        }
                    }
                }
            }
            is Playlist -> playlistRepository.remove(playback.mediaId)
        }

        if (playback != null) {
            historyRepository.removeHierarchical(playback.mediaId)
        }
    }
}