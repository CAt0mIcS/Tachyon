package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.ObserveSavedData
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.main.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.normalize
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    playbackRepository: PlaybackRepository,
    private val getRepositoryStates: GetRepositoryStates,
    private val normalizeCurrentPosition: NormalizeCurrentPosition,
    observeSavedData: ObserveSavedData,
    private val pauseResumePlayback: PauseResumePlayback,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val playPlayback: PlayPlayback
) : ViewModel() {

    val playback = playbackRepository.historyFlow.map { history ->
        history.find { it.isPlayable.value }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Lazily, null)

    val isPlaying = getRepositoryStates.isPlaying()

    var audioUpdateInterval: Duration = SettingsEntity().audioUpdateInterval
        private set

    private var recentlyPlayedPosition = 0f

    init {
        observeSavedData().onEach {
            recentlyPlayedPosition =
                it.currentPositionInRecentlyPlayedPlayback.normalize(it.recentlyPlayedDuration)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    // TODO: Jumps around when isPlaying state switches
    fun getCurrentPositionNormalized(): Float =
        if (getRepositoryStates.isPlaying().value) normalizeCurrentPosition() ?: 0f
        else recentlyPlayedPosition

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else {
            viewModelScope.launch(Dispatchers.IO) {
                val recentlyPlayed = getRecentlyPlayed()
                runOnUiThreadAsync {
                    playPlayback(playback.value, recentlyPlayed?.position)
                }
            }
        }
    }
}