package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddSongToExcludedSongs(
    private val settingsRepository: SettingsRepository,
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(song: Song) = withContext(Dispatchers.IO) {
        settingsRepository.addExcludedFilesRange(listOf(song.uri))
        songRepository.updateIsHidden(song.mediaId, isHidden = true)

        // TODO: Decide below

        // We don't want to remove the hidden song from history
//        historyRepository.removeHierarchical(song.mediaId)

        // We don't want to remove remixes using the hidden song
//        remixRepository.removeIf {
//            it.mediaId.underlyingMediaId == song.mediaId
//        }


        // We don't want to remove the hidden song from existing playlists
//        val playlists = playbackRepository.getPlaylists()
//        for (i in playlists.indices) {
//            playlists[i].apply {
//                if (hasPlayback(song)) {
//                    remove(song)
//                    playlistRepository.setPlaybacksOfPlaylist(mediaId, playbacks.map { it.mediaId })
//                }
//            }
//        }
    }
}