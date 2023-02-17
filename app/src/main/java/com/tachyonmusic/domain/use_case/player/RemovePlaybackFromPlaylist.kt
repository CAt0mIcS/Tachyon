package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.PlaylistRepository

class RemovePlaybackFromPlaylist(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playback: SinglePlayback?, i: Int) {
        if (playback == null)
            return

        val playlist = playlistRepository.getPlaylists().getOrNull(i)
        if (playlist == null || !playlist.hasPlayback(playback))
            return

        playlist.remove(playback)
        playlistRepository.setPlaybacksOfPlaylist(
            playlist.mediaId,
            playlist.playbacks.map { it.mediaId })
    }
}