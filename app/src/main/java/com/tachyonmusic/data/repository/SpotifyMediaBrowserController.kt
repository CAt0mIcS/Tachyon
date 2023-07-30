package com.tachyonmusic.data.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.cycle
import com.tachyonmusic.util.indexOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// TODO: Handle case where client is not authorized to use Spotify
class SpotifyMediaBrowserController(
    private val api: SpotifyInterfacer
) : IListenable<MediaBrowserController.EventListener> by Listenable() {

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    val currentPlayback = api.currentPlayback

    val isPlaying = api.isPlaying

    private var repeatModeOnceAuthorized: RepeatMode? = null

    override fun registerEventListener(listener: MediaBrowserController.EventListener) {
        api.registerEventListener(listener)
    }

    override fun unregisterEventListener(listener: MediaBrowserController.EventListener) {
        api.unregisterEventListener(listener)
    }

    fun setPlaylist(playlist: Playlist, position: Duration?) {
        if (!api.isAuthorized)
            return

        api.play(playlist.mediaId.source, correctIndex(playlist, playlist.currentPlaylistIndex))
        _currentPlaylist.update { playlist }
        api.seekTo(position ?: return)
    }

    val currentPosition: Duration?
        get() = if (api.isAuthorized) api.currentPosition else null

    var currentPlaybackTimingData: TimingDataController?
        get() = TimingDataController()
        set(value) {}


    val nextPlayback: SinglePlayback?
        get() {
            // TODO: Doesn't account for shuffle
            return currentPlaylist.value?.playbacks?.cycle(
                (currentPlaylist.value?.currentPlaylistIndex ?: return null) + 1
            )
        }

    val repeatMode = api.repeatMode

    fun setRepeatMode(repeatMode: RepeatMode) {
        if (!api.isAuthorized) {
            repeatModeOnceAuthorized = repeatMode
            return
        }
        api.setRepeatMode(repeatMode)
    }

    fun play(playback: SinglePlayback) {
        if (!api.isAuthorized)
            return

        if (repeatModeOnceAuthorized != null) {
            api.setRepeatMode(repeatModeOnceAuthorized!!)
            repeatModeOnceAuthorized = null
        }
        api.play(playback.mediaId.source)
    }

    fun play() {
        if (!api.isAuthorized)
            return

        if (repeatModeOnceAuthorized != null) {
            api.setRepeatMode(repeatModeOnceAuthorized!!)
            repeatModeOnceAuthorized = null
        }
        api.resume()
    }

    fun pause() {
        if (!api.isAuthorized)
            return
        api.pause()
    }

    fun seekTo(pos: Duration?) {
        if (!api.isAuthorized)
            return
        api.seekTo(pos ?: return)
    }

    fun seekTo(mediaId: MediaId, pos: Duration?) {
        if (!api.isAuthorized)
            return

        val index = correctIndex(
            currentPlaylist.value ?: return,
            currentPlaylist.value?.playbacks?.indexOf { it.mediaId == mediaId } ?: return
        )

        api.seekTo(
            currentPlaylist.value?.mediaId?.source ?: return,
            index,
            pos
        )
    }

    fun seekTo(index: Int, pos: Duration?) {
        if (!api.isAuthorized)
            return
        api.seekTo(
            currentPlaylist.value?.mediaId?.source ?: return,
            index,
            pos ?: return
        )
    }

    private fun correctIndex(playlist: Playlist, index: Int): Int {
        /**
         * In [PlayPlayback] the [Playlist.currentPlaylistIndex] is corrected to avoid trying to
         * play a non-playable item. Spotify does this automatically so we need to reverse the correction
         */
        val notPlayableItems = playlist.playbacks.filter { !it.isPlayable }.size
        return if (index - notPlayableItems < 0)
            index - notPlayableItems + playlist.playbacks.size
        else
            index - notPlayableItems
    }
}