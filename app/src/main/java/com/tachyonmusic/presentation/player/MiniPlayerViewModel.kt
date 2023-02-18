package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.main.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayRecentlyPlayed
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.normalize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val normalizeCurrentPosition: NormalizeCurrentPosition,
    private val getHistory: GetHistory,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playRecentlyPlayed: PlayRecentlyPlayed,
    private val browser: MediaBrowserController // TODO: Shouldn't be in ViewModel
) : ViewModel() {

    private val _playback = MutableStateFlow<Song?>(null)
    val playback = _playback.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    var audioUpdateInterval: Duration = SettingsEntity().audioUpdateInterval
        private set

    private var recentlyPlayedPosition = 0f
    private val mediaListener = MediaListener()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val recentlyPlayedPlayback = getHistory().firstOrNull()

            _playback.update { recentlyPlayedPlayback?.underlyingSong }
            loadArtworkAsync()

            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPosition =
                recentlyPlayed?.position?.normalize(recentlyPlayed.duration) ?: 0f
        }
    }

    fun registerMediaListener() {
        browser.registerEventListener(mediaListener)
    }

    fun unregisterMediaListener() {
        browser.unregisterEventListener(mediaListener)
    }

    fun getCurrentPositionNormalized() = normalizeCurrentPosition() ?: recentlyPlayedPosition

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else {
            viewModelScope.launch(Dispatchers.IO) {
                playRecentlyPlayed(playback.value)
            }
        }
    }

    private fun loadArtworkAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(playback.value ?: return@launch).onEach { res ->
                playback.value?.artwork?.update { res.data?.artwork }
                playback.value?.isArtworkLoading?.update { false }
            }.collect()
        }
    }


    private inner class MediaListener : MediaBrowserController.EventListener {
        override fun onPlaybackTransition(
            playback: SinglePlayback?,
            associatedPlaylist: Playlist?
        ) {
            _playback.value = playback?.underlyingSong
            loadArtworkAsync()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }
}