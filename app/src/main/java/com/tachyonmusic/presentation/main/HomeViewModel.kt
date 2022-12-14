package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.main.NormalizePosition
import com.tachyonmusic.domain.use_case.main.GetPagedHistory
import com.tachyonmusic.domain.use_case.main.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.main.UnloadArtworks
import com.tachyonmusic.domain.use_case.main.UpdateArtworks
import com.tachyonmusic.domain.use_case.main.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayerListenerHandler
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getHistory: GetPagedHistory,
    private val playerListener: PlayerListenerHandler,
    val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val pauseResumePlayback: PauseResumePlayback,
    private val normalizePosition: NormalizePosition,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,
    private val updateArtworks: UpdateArtworks,
    private val unloadArtworks: UnloadArtworks,
    private val getRecentlyPlayed: GetRecentlyPlayed
) : ViewModel() {

    val isPlaying = playerListener.isPlaying
    val currentPositionNormalized: Float?
        get() = normalizePosition()
    var recentlyPlayedPositionNormalized: Float = 0f
        private set

    var history = getHistory(5)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPositionNormalized = normalizePosition(
                recentlyPlayed.positionMs,
                recentlyPlayed.durationMs
            )

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
        else {
            viewModelScope.launch(Dispatchers.IO) {
                if (!setCurrentPlaybackToRecentlyPlayed(playback, playWhenReady = true)) {
                    runOnUiThreadAsync {
                        pauseResumePlayback(PauseResumePlayback.Action.Resume)
                    }
                }
            }
        }
    }

    fun onMiniPlayerClicked(playback: Playback?) {
        viewModelScope.launch(Dispatchers.IO) {
            setCurrentPlaybackToRecentlyPlayed(playback)
        }
    }

    private suspend fun setCurrentPlaybackToRecentlyPlayed(
        playback: Playback?,
        playWhenReady: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val recentlyPlayedPos = getRecentlyPlayed().positionMs
        runOnUiThread {
            setCurrentPlayback(playback, playWhenReady, recentlyPlayedPos)
        }
    }

    fun refreshArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            unloadArtworks()
            updateArtworks(ignoreIsFirstAppStart = true)
        }
    }
}