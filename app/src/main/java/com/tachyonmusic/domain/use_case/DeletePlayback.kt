package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeletePlayback(
    private val customizedSongRepository: CustomizedSongRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(playback: Playback?) = withContext(Dispatchers.IO) {
        when (playback) {
            is CustomizedSong -> {
                customizedSongRepository.remove(playback.mediaId)

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