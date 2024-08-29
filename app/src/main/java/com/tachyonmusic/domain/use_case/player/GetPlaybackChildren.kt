package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
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
    @JvmName("invokePlayback")
    operator fun invoke(
        playback: Playback?,
        repeatMode: RepeatMode,
        currentPlaylistMediaId: MediaId?
    ): List<Playback> {
        log.debug("Getting children for $playback with $repeatMode")

        if (playback == null)
            return emptyList()

        return when (repeatMode) {
            is RepeatMode.One -> listOf(playback)
            is RepeatMode.All -> getPlaylistAll(playback)
            is RepeatMode.Shuffle -> getPlaylistShuffle()
            is RepeatMode.Off -> getPlaylistOff(playback, currentPlaylistMediaId)
        }
    }

    @JvmName("invokePlaylist")
    operator fun invoke(playlist: Playlist?) = playlist?.playbacks

    private fun getPlaylistAll(playback: Playback): List<Playback> {
        return if (playback.isSong) {
            val idx =
                predefinedPlaylists.songPlaylist.value.indexOf { playback.mediaId == it.mediaId }
                    ?: return emptyList()
            listOfNotNull(predefinedPlaylists.songPlaylist.value.cycle(idx + 1))
        } else if (playback.isRemix) {
            val idx =
                predefinedPlaylists.remixPlaylist.value.indexOf { playback.mediaId == it.mediaId }
                    ?: return emptyList()
            listOfNotNull(predefinedPlaylists.remixPlaylist.value.cycle(idx + 1))
        } else
            TODO("Invalid playback type ${playback.javaClass.name}")
    }

    private fun getPlaylistShuffle() =
        if (browser.nextPlayback != null) listOf(browser.nextPlayback!!) else emptyList()

    private fun getPlaylistOff(
        playback: Playback,
        currentPlaylistMediaId: MediaId?
    ): List<Playback> {
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
