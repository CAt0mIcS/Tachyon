package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.playback_layers.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddSongToExcludedSongs(
    private val settingsRepository: SettingsRepository,
    private val songRepository: SongRepository,
    private val historyRepository: HistoryRepository,
    private val loopRepository: LoopRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(song: Song) = withContext(Dispatchers.IO) {
        settingsRepository.addExcludedFilesRange(listOf(song.uri))
        songRepository.remove(song.mediaId)
        historyRepository.removeHierarchical(song.mediaId)
        loopRepository.removeIf {
            it.mediaId.underlyingMediaId == song.mediaId
        }

        val playlists = playbackRepository.getPlaylists()
        for (i in playlists.indices) {
            playlists[i].apply {
                if (hasPlayback(song)) {
                    remove(song)
                    playlistRepository.setPlaybacksOfPlaylist(mediaId, playbacks.map { it.mediaId })
                }
            }
        }
    }
}