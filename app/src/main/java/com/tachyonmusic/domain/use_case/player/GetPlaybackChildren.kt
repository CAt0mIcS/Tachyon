package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.media.core.RepeatMode
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.util.Resource

/**
 * For songs and loops:
 * * Finds the next playback in all the songs/loops depending on the repeat mode
 *
 * For playlists:
 * * Returns all items in the playlist as children
 */
class GetPlaybackChildren(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback
) {
    suspend operator fun invoke(playback: Playback?, repeatMode: RepeatMode): List<SinglePlayback> {
        if (playback == null)
            return emptyList()

        return when (repeatMode) {
            RepeatMode.All -> repeatModeAll(playback)
            RepeatMode.One -> repeatModeOne(playback)
            RepeatMode.Shuffle -> repeatModeShuffle(playback)
        }
    }

    private suspend fun repeatModeAll(playback: Playback): List<SinglePlayback> {
        if (playback is Playlist)
            return playback.playbacks

        val playlist = getPlaylist(playback)

        val pbIdx = playlist.indexOfFirst { it.mediaId == playback.mediaId }
        if (pbIdx == -1)
            return emptyList()
        if (pbIdx + 1 >= playlist.size)
            return listOf(playlist[0])

        return listOf(playlist[pbIdx + 1])
    }

    private fun repeatModeOne(playback: Playback) =
        if (playback is SinglePlayback)
            listOf(playback)
        else
            (playback as Playlist).playbacks

    private suspend fun repeatModeShuffle(playback: Playback): List<SinglePlayback> {
        if(playback is Playlist)
            return playback.playbacks

        return listOf(
            getPlaylist(playback).getOrNull(browser.nextMediaItemIndex) ?: return emptyList()
        )
    }


    private suspend fun getPlaylist(playback: Playback): List<SinglePlayback> {
        /**
         * TODO: We only need [GetPlaylistForPlayback.ActivePlaylist.playbackItems]. Optimize
         *  so that we don't create [GetPlaylistForPlayback.ActivePlaylist.mediaItems]
         */
        val playlistRes = getPlaylistForPlayback(playback)

        if (playlistRes is Resource.Error || playlistRes.data == null)
            return emptyList()

        return playlistRes.data!!.playbackItems
    }
}