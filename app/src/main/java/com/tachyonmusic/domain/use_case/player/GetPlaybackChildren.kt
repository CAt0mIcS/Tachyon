package com.tachyonmusic.domain.use_case.player

import android.content.Context
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.setPlayableState
import kotlinx.coroutines.flow.update

/**
 * For songs and loops:
 * * Finds the next playback in all the songs/loops depending on the repeat mode
 *
 * For playlists:
 * * Returns all items in the playlist as children
 */
class GetPlaybackChildren(
    private val browser: MediaBrowserController,
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val context: Context
) {
    suspend operator fun invoke(
        playback: Playback?,
        repeatMode: RepeatMode,
        sortParams: SortParameters
    ): List<SinglePlayback> {
        if (playback == null)
            return emptyList()

        return when (repeatMode) {
            RepeatMode.All -> repeatModeAll(playback, sortParams)
            RepeatMode.One -> repeatModeOne(playback)
            RepeatMode.Shuffle -> repeatModeShuffle(playback, sortParams)
        }
    }

    private suspend fun repeatModeAll(
        playback: Playback,
        sortParams: SortParameters
    ): List<SinglePlayback> {
        if (playback is Playlist)
            return playback.playbacks.setPlayableState(context)

        val playlist = getPlaylist(playback, sortParams)

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
            (playback as Playlist).playbacks.setPlayableState(context)

    private suspend fun repeatModeShuffle(
        playback: Playback,
        sortParams: SortParameters
    ): List<SinglePlayback> {
        if (playback is Playlist)
            return playback.playbacks.setPlayableState(context)

        return listOf(
            getPlaylist(
                playback,
                sortParams
            ).getOrNull(runOnUiThread { browser.nextMediaItemIndex })
                ?: return emptyList()
        )
    }


    private suspend fun getPlaylist(
        playback: Playback,
        sortParams: SortParameters
    ): List<SinglePlayback> {
        /**
         * TODO: We only need [GetPlaylistForPlayback.ActivePlaylist.playbackItems]. Optimize
         *  so that we don't create [GetPlaylistForPlayback.ActivePlaylist.mediaItems]
         *
         * TODO: Currently runs on UI thread due to bug when switching from [RepeatMode.All] to [RepeatMode.Shuffle]
         */
        val playlistRes = getPlaylistForPlayback(playback, sortParams)

        if (playlistRes is Resource.Error || playlistRes.data == null)
            return emptyList()

        return playlistRes.data!!.playbackItems
    }
}