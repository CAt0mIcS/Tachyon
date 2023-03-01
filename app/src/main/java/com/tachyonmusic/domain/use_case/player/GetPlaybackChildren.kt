package com.tachyonmusic.domain.use_case.player

import android.content.Context
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.media.util.playback
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.setPlayableState

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

        if (playback !is SinglePlayback)
            return (playback as Playlist).playbacks.setPlayableState(context)

        return when (repeatMode) {
            RepeatMode.All -> repeatModeAll(playback, sortParams)
            RepeatMode.One -> repeatModeOne(playback)
            RepeatMode.Shuffle -> repeatModeShuffle()
        }
    }

    private suspend fun repeatModeAll(
        playback: SinglePlayback,
        sortParams: SortParameters
    ): List<SinglePlayback> {
        return nextItem() ?: resolveNextItem(playback, sortParams) ?: emptyList()
    }

    private fun repeatModeOne(playback: SinglePlayback) = listOf(playback)

    private suspend fun repeatModeShuffle() = nextItem() ?: emptyList()


    private suspend fun resolveNextItem(
        playback: SinglePlayback,
        sortParams: SortParameters
    ): List<SinglePlayback>? {
        val playlistRes = getPlaylistForPlayback(playback, sortParams)

        if (playlistRes is Resource.Error || playlistRes.data == null)
            return null

        val playlist = playlistRes.data!!.playbackItems

        val pbIdx = playlist.indexOfFirst { it.mediaId == playback.mediaId }
        if (pbIdx == -1)
            return null
        if (pbIdx + 1 >= playlist.size)
            return listOf(playlist[0]).setPlayableState(context)

        return listOf(playlist[pbIdx + 1]).setPlayableState(context)
    }


    private suspend fun nextItem() = runOnUiThread {
        val next = browser.getMediaItemAt(browser.nextMediaItemIndex)?.mediaMetadata?.playback
            ?: return@runOnUiThread null
        listOf(next).setPlayableState(context)
    }
}