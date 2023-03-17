package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.GetMediaStates
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.main.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.playback_layers.PlaybackRepository
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.normalize
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    playbackRepository: PlaybackRepository,
    getMediaStates: GetMediaStates,
    private val normalizeCurrentPosition: NormalizeCurrentPosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playPlayback: PlayPlayback
) : ViewModel() {

    val playback = playbackRepository.historyFlow.map { history ->
        history.find { it.isPlayable.value }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Lazily, null)

    val isPlaying = getMediaStates.isPlaying()

    var audioUpdateInterval: Duration = SettingsEntity().audioUpdateInterval
        private set

    private var recentlyPlayedPosition = 0f

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPosition =
                recentlyPlayed?.position?.normalize(recentlyPlayed.duration) ?: 0f
        }
    }

    fun getCurrentPositionNormalized() = normalizeCurrentPosition() ?: recentlyPlayedPosition

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