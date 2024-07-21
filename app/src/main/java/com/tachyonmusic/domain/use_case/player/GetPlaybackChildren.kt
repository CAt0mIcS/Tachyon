package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.predefinedRemixPlaylistMediaId
import com.tachyonmusic.playback_layers.predefinedSongPlaylistMediaId
import com.tachyonmusic.util.cycle
import com.tachyonmusic.util.indexOf

class GetPlaybackChildren(
    private val browser: MediaBrowserController,
    private val predefinedPlaylists: PredefinedPlaylistsRepository,
    private val log: Logger
) {
    operator fun invoke(
        playback: Playback?,
        repeatMode: RepeatMode,
        currentPlaylistMediaId: MediaId?
    ): List<SinglePlayback> {
        log.debug("Getting children for $playback with $repeatMode")

        if (playback == null)
            return emptyList()

        return when (playback) {
            is SinglePlayback -> {
                when (repeatMode) {
                    is RepeatMode.One -> listOf(playback)
                    is RepeatMode.All -> getPlaylistAll(playback)
                    is RepeatMode.Shuffle -> getPlaylistShuffle()
                    is RepeatMode.Off -> getPlaylistOff(playback, currentPlaylistMediaId)
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
                    predefinedPlaylists.songPlaylist.value.indexOf { playback.mediaId == it.mediaId }
                        ?: return emptyList()
                listOfNotNull(predefinedPlaylists.songPlaylist.value.cycle(idx + 1))
            }
            is Remix -> {
                val idx =
                    predefinedPlaylists.remixPlaylist.value.indexOf { playback.mediaId == it.mediaId }
                        ?: return emptyList()
                listOfNotNull(predefinedPlaylists.remixPlaylist.value.cycle(idx + 1))
            }
            else -> TODO("Invalid playback type ${playback.javaClass.name}")
        }
    }

    private fun getPlaylistShuffle() =
        if (browser.nextPlayback != null) listOf(browser.nextPlayback!!) else emptyList()

    private fun getPlaylistOff(
        playback: SinglePlayback,
        currentPlaylistMediaId: MediaId?
    ): List<SinglePlayback> {
        when (currentPlaylistMediaId) {
            predefinedSongPlaylistMediaId -> {
                if (predefinedPlaylists.songPlaylist.value.lastOrNull()?.mediaId == playback.mediaId)
                    return emptyList()
            }
            predefinedRemixPlaylistMediaId -> {
                if (predefinedPlaylists.remixPlaylist.value.lastOrNull()?.mediaId == playback.mediaId)
                    return emptyList()
            }
        }

        return getPlaylistAll(playback)
    }
}
