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
import com.tachyonmusic.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

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
    ) = withContext(Dispatchers.IO) {
        if (playback == null)
            return@withContext emptyList()

        if (playback !is SinglePlayback)
            return@withContext (playback as Playlist).playbacks.setPlayableState(context)

        when (repeatMode) {
            RepeatMode.All -> nextItem() ?: resolveNextItem(playback, sortParams) ?: emptyList()
            RepeatMode.One -> listOf(playback)
            RepeatMode.Shuffle -> nextItem() ?: emptyList()
        }
    }


    /**
     * Uses the playlist which will be used by the player once started
     */
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


    /**
     * Uses the index of the next playing media item. Only works if playback is already started
     */
    private suspend fun nextItem() = runOnUiThread {
        val next = browser.getMediaItemAt(browser.nextMediaItemIndex)?.mediaMetadata?.playback
            ?: return@runOnUiThread null

        listOf(next).setPlayableState(context)
    }
}