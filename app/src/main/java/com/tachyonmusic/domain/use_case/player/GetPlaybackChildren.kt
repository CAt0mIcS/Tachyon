package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.PredefinedPlaylistsRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.cycle
import com.tachyonmusic.util.indexOf

class GetPlaybackChildren(
    private val browser: MediaBrowserController,
    private val predefinedPlaylists: PredefinedPlaylistsRepository,
    private val log: Logger
) {
    operator fun invoke(playback: Playback?, repeatMode: RepeatMode): List<SinglePlayback> {
        log.debug("Getting children for $playback with $repeatMode")

        if (playback == null)
            return emptyList()

        return when (playback) {
            is SinglePlayback -> {
                when (repeatMode) {
                    is RepeatMode.One -> listOf(playback)
                    is RepeatMode.All -> getPlaylistAll(playback)
                    is RepeatMode.Shuffle -> getPlaylistShuffle()
                }
            }

            is Playlist -> playback.playbacks
            else -> emptyList()
        }
    }

    private fun getPlaylistAll(playback: SinglePlayback): List<SinglePlayback> {
        return when (playback) {
            is Song -> {
                val idx =
                    predefinedPlaylists.songPlaylist.indexOf { playback.mediaId == it.mediaId }
                        ?: return emptyList()
                listOfNotNull(predefinedPlaylists.songPlaylist.cycle(idx + 1))
            }
            is CustomizedSong -> {
                val idx =
                    predefinedPlaylists.customizedSongPlaylist.indexOf { playback.mediaId == it.mediaId }
                        ?: return emptyList()
                listOfNotNull(predefinedPlaylists.customizedSongPlaylist.cycle(idx + 1))
            }
            else -> emptyList()
        }
    }

    private fun getPlaylistShuffle() =
        if (browser.nextPlayback != null) listOf(browser.nextPlayback!!) else emptyList()
}