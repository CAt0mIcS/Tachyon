package com.tachyonmusic.presentation.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.HandleCurrentPlaybackState
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.main.GetPagedHistory
import com.tachyonmusic.domain.use_case.main.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.main.NormalizePosition
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.HandleLoopState
import com.tachyonmusic.domain.use_case.player.HandlePlaybackState
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayerListenerHandler
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.presentation.player.data.RepeatMode
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Long.max
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerListener: PlayerListenerHandler,
    private val getCurrentPosition: GetCurrentPosition,
    val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val handlePlaybackState: HandlePlaybackState,
    private val handleLoopState: HandleLoopState,
    private val seekToPosition: SeekToPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString,
    private val itemClicked: ItemClicked,
    private val pauseResumePlayback: PauseResumePlayback,
    private val handleCurrentPlaybackState: HandleCurrentPlaybackState,
    private val normalizePosition: NormalizePosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val getHistory: GetPagedHistory
) : ViewModel(), MediaBrowserController.EventListener {

    val isPlaying = playerListener.isPlaying

    val currentPosition: Long
        get() = getCurrentPosition()

    val playbackState = handlePlaybackState.playbackState
    val loopState = handleLoopState.loopState

    private var _repeatMode = mutableStateOf<RepeatMode>(RepeatMode.One)
    val repeatMode: State<RepeatMode> = _repeatMode

    val playback = handleCurrentPlaybackState.currentPlayback

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
        }
    }


    fun registerPlayerListeners() {
        handlePlaybackState.register()
        handleLoopState.register()
        playerListener.register()
        handleCurrentPlaybackState.register()
    }

    fun unregisterPlayerListeners() {
        handlePlaybackState.unregister()
        handleLoopState.unregister()
        playerListener.unregister()
        handleCurrentPlaybackState.unregister()
    }

    fun getTextForPosition(position: Long) = millisecondsToReadableString(position)

    fun onSeekTo(position: Long) {
        seekToPosition(position)
    }

    fun onItemClicked(playback: Playback?) {
        itemClicked(playback)
    }

    // TODO: Don't hard-code 10000 back/forward seek time, should be a user setting
    // TODO: Use Player.onSeek(Back/Forward)IncrementChanged...
    fun onSeekBack() {
        seekToPosition(max(currentPosition - 10000, 0L))
    }

    fun onSeekForward() {
        seekToPosition(min(currentPosition + 10000, playbackState.value.duration))
    }

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun onRepeatModeChange() {
        _repeatMode.value = repeatMode.value.next
    }


    fun onMiniPlayerPlayPauseClicked() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
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
}