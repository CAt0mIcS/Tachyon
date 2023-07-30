package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.ObserveSavedData
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.PlaybackLocation
import com.tachyonmusic.domain.use_case.home.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.isPredefined
import com.tachyonmusic.presentation.core_components.model.toUiEntity
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.normalize
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    playbackRepository: PlaybackRepository,
    loadArtworkForPlayback: LoadArtworkForPlayback,
    private val getRepositoryStates: GetRepositoryStates,
    private val normalizeCurrentPosition: NormalizeCurrentPosition,
    observeSavedData: ObserveSavedData,
    private val pauseResumePlayback: PauseResumePlayback,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val playPlayback: PlayPlayback
) : ViewModel() {

    private val _playback = playbackRepository.historyFlow.map { history ->
        history.find { it.isPlayable }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val playback = _playback.map {
        loadArtworkForPlayback(it ?: return@map null).toUiEntity()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

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
                val currentPlaylist =getRepositoryStates.currentPlaylist().value
                val playbackType = if (currentPlaylist?.isPredefined == true)
                    PlaybackType.build(_playback.value)
                else
                    PlaybackType.build(currentPlaylist)
                runOnUiThreadAsync {
                    playPlayback(
                        _playback.value,
                        recentlyPlayed?.position,
                        if (playbackType is PlaybackType.Playlist) PlaybackLocation.CUSTOM_PLAYLIST else null
                    )
                }
            }
        }
    }
}