package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.predefinedSongPlaylistMediaId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

class AddSongToExcludedSongs(
    private val settingsRepository: SettingsRepository,
    private val songRepository: SongRepository,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val mediaBrowser: MediaBrowserController
) {
    suspend operator fun invoke(playback: Playback) = withContext(Dispatchers.IO) {
        assert(playback.isSong)

        settingsRepository.addExcludedFilesRange(listOf(playback.uri!!))
        songRepository.updateIsHidden(playback.mediaId, isHidden = true)

        /**
         * Update media browser to make sure current playlist does not include deleted playback
         */
        predefinedPlaylistsRepository.songPlaylist.takeWhile { songs ->
            if(songs.find { it.mediaId == playback.mediaId } == null) {
                mediaBrowser.updatePredefinedPlaylist()
                false
            }
            else
                true
        }.collect()

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