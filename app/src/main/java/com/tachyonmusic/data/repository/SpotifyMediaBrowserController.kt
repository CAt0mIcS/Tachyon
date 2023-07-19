package com.tachyonmusic.data.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// TODO: Handle case where client is not authorized to use Spotify
class SpotifyMediaBrowserController(
    private val api: SpotifyInterfacer
): IListenable<MediaBrowserController.EventListener> by Listenable() {

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    val currentPlayback = api.currentPlayback

    val isPlaying = api.isPlaying

    override fun registerEventListener(listener: MediaBrowserController.EventListener) {
        api.registerEventListener(listener)
    }

    override fun unregisterEventListener(listener: MediaBrowserController.EventListener) {
        api.unregisterEventListener(listener)
    }

    fun setPlaylist(playlist: Playlist, position: Duration?) {
        if (!api.isAuthorized)
            return

        api.play(playlist.mediaId.source, playlist.currentPlaylistIndex)
        api.seekTo(position ?: return)

        _currentPlaylist.update { playlist }
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
        if (!api.isAuthorized)
            return
        api.setRepeatMode(repeatMode)
    }

    fun play(playback: SinglePlayback) {
        if (!api.isAuthorized)
            return
        api.play(playback.mediaId.source)
    }

    fun play() {
        if (!api.isAuthorized)
            return
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
        api.seekTo(
            currentPlaylist.value?.mediaId?.source ?: return,
            currentPlaylist.value?.playbacks?.indexOf { it.mediaId == mediaId } ?: return,
            pos ?: return
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
}