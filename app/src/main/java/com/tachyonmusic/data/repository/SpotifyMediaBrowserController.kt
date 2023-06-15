package com.tachyonmusic.data.repository

import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.cycle
import com.tachyonmusic.util.indexOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SpotifyMediaBrowserController(
    private val api: SpotifyInterfacer
) {

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    private val _currentPlayback = MutableStateFlow<SinglePlayback?>(null)
    val currentPlayback: StateFlow<SinglePlayback?> = _currentPlayback.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun setPlaylist(playlist: Playlist, position: Duration?) {
        api.play(playlist.mediaId.source, playlist.currentPlaylistIndex)
        api.seekTo(position ?: return)

        _currentPlaylist.update { playlist }
        _currentPlayback.update { playlist.current }
    }

    val currentPosition: Duration?
        get() = api.currentPosition

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

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.All)
    val repeatMode = _repeatMode.asStateFlow()

    fun setRepeatMode(repeatMode: RepeatMode) {
        api.setRepeatMode(repeatMode)
        _repeatMode.update { repeatMode }
    }

    fun play(playback: SinglePlayback) {
        api.play(playback.mediaId.source)
    }

    fun play() {
        api.resume()
    }

    fun pause() {
        api.pause()
    }

    fun seekTo(pos: Duration?) {
        api.seekTo(pos ?: return)
    }

    fun seekTo(mediaId: MediaId, pos: Duration?) {
        api.seekTo(
            currentPlaylist.value?.mediaId?.source ?: return,
            currentPlaylist.value?.playbacks?.indexOf { it.mediaId == mediaId } ?: return,
            pos ?: return
        )
    }

    fun seekTo(index: Int, pos: Duration?) {
        api.seekTo(
            currentPlaylist.value?.mediaId?.source ?: return,
            index,
            pos ?: return
        )
    }
}