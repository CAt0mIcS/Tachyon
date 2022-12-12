package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayerListenerHandler
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getHistory: GetPagedHistory,
    private val playerListener: PlayerListenerHandler,
    private val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val pauseResumePlayback: PauseResumePlayback,
    private val getCurrentPositionNormalized: GetCurrentPositionNormalized,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,
    private val updateArtworks: UpdateArtworks,
    private val unloadArtworks: UnloadArtworks
) : ViewModel() {

    val isPlaying = playerListener.isPlaying
    val currentPositionNormalized: Float
        get() = getCurrentPositionNormalized()
    val audioUpdateInterval: Duration
        get() = getAudioUpdateInterval()

    var history = getHistory(5)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsDatabase()
            updateSongDatabase()
            updateArtworks()
        }
    }

    fun registerPlayerListener() {
        playerListener.register()
    }

    fun unregisterPlayerListener() {
        playerListener.unregister()
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }

    fun onPlayPauseClicked(playback: Playback?) {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else if (!setCurrentPlayback(playback))
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun onMiniPlayerClicked(playback: Playback?) {
        setCurrentPlayback(playback, false)
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
            updateArtworks(ignoreIsFirstAppStart = true)
        }
    }
}