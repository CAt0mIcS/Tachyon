package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.domain.use_case.GetMediaStates
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.main.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayRecentlyPlayed
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.normalize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    private val getHistory: GetHistory,
    private val normalizeCurrentPosition: NormalizeCurrentPosition,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val pauseResumePlayback: PauseResumePlayback,
    private val playRecentlyPlayed: PlayRecentlyPlayed,
) : ViewModel() {

    val playback = getMediaStates.playback().map {
        it ?: getHistory().firstOrNull()
    }.onEach { singlePb ->
        if (singlePb == null)
            return@onEach

        getOrLoadArtwork(singlePb.underlyingSong).onEach { res ->
            when (res) {
                is Resource.Loading -> singlePb.isArtworkLoading.update { true }
                else -> {
                    singlePb.artwork.update { res.data!!.artwork }
                    singlePb.isArtworkLoading.update { false }
                }
            }
        }.collect()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Lazily, null)

    val isPlaying = getMediaStates.playWhenReady()

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
                playRecentlyPlayed(playback.value)
            }
        }
    }
}