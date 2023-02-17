package com.tachyonmusic.presentation.player

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.GetHistory
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.ItemClicked
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.main.NormalizePosition
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.GetNextPlaybackItems
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.SeekPosition
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.presentation.player.data.ArtworkState
import com.tachyonmusic.presentation.player.data.PlaybackState
import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.use_case.ObservePlaylists
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewPlaylist
import com.tachyonmusic.domain.use_case.player.RemovePlaybackFromPlaylist
import com.tachyonmusic.domain.use_case.player.SavePlaybackToPlaylist
import com.tachyonmusic.presentation.player.data.SeekIncrementsState
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getCurrentPosition: GetCurrentPosition,
    private val seekPosition: SeekPosition,
    private val millisecondsToReadableString: MillisecondsToReadableString,
    private val itemClicked: ItemClicked,
    private val pauseResumePlayback: PauseResumePlayback,
    private val normalizePosition: NormalizePosition,
    private val getRecentlyPlayed: GetRecentlyPlayed,
    private val setCurrentPlayback: SetCurrentPlayback,
    private val getOrLoadArtwork: GetOrLoadArtwork,
    private val getNextPlaybackItems: GetNextPlaybackItems,
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
    private val savePlaybackToPlaylist: SavePlaybackToPlaylist,
    private val removePlaybackFromPlaylist: RemovePlaybackFromPlaylist,
    private val createAndSaveNewPlaylist: CreateAndSaveNewPlaylist,
    private val browser: MediaBrowserController,  // TODO: Shouldn't be used in ViewModel
    getHistory: GetHistory,
    observeSettings: ObserveSettings,
    observePlaylists: ObservePlaylists
) : ViewModel() {

    private var _repeatMode = mutableStateOf<RepeatMode>(RepeatMode.One)
    val repeatMode: State<RepeatMode> = _repeatMode

    private var _recentlyPlayed = mutableStateOf<Playback?>(null)
    val recentlyPlayed: State<Playback?> = _recentlyPlayed

    private var _isPlaying = mutableStateOf(browser.isPlaying)
    val isPlaying: State<Boolean> = _isPlaying

    private var _playbackState = mutableStateOf(PlaybackState())
    val playbackState: State<PlaybackState> = _playbackState

    private var _artworkState = mutableStateOf(ArtworkState())
    val artworkState: State<ArtworkState> = _artworkState

    private var _seekIncrement = mutableStateOf(SeekIncrementsState())
    val seekIncrement: State<SeekIncrementsState> = _seekIncrement

    val timingData = mutableStateListOf<TimingData>()
//    val timingData: State<List<TimingData>> = _timingData

    private var _currentTimingDataIndex = mutableStateOf(0)
    val currentTimingDataIndex: State<Int> = _currentTimingDataIndex

    val playlists = mutableStateListOf<Pair<String, Boolean>>()

    var audioUpdateInterval: Duration = 100.milliseconds
        private set

    val currentPosition: Long
        get() = getCurrentPosition() ?: recentlyPlayedPosition

    val currentPositionNormalized: Float?
        get() = normalizePosition()

    var recentlyPlayedPositionNormalized: Float = 0f
        private set

    private var recentlyPlayedPosition: Long = 0L
    private var showMillisecondsInPositionText: Boolean = false
    private val mediaListener = MediaListener()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val recentlyPlayedPlayback = getHistory().firstOrNull()

            runOnUiThreadAsync {
                _recentlyPlayed.value = recentlyPlayedPlayback
                updatePlaybackState(recentlyPlayed.value)

                observePlaylists().map { newPlaylists ->
                    playlists.clear()
                    // TODO: Check if we're trying to save a playlist to a playlist and if [recentlyPlayed.value] is null
                    playlists.addAll(newPlaylists.map {
                        it.name to it.hasPlayback(recentlyPlayed.value as SinglePlayback)
                    })
                }.collect()
            }
            if (recentlyPlayedPlayback != null)
                getOrLoadArtworkForPlayback(recentlyPlayedPlayback)

            val recentlyPlayed = getRecentlyPlayed()
            recentlyPlayedPositionNormalized = normalizePosition(
                recentlyPlayed?.positionMs ?: 0L,
                recentlyPlayed?.durationMs ?: 0L
            )
            recentlyPlayedPosition = recentlyPlayed?.positionMs ?: 0L

            runOnUiThreadAsync {
                timingData.clear()
                timingData.add(TimingData(0L, recentlyPlayed?.durationMs ?: 0L))
            }
        }

        observeSettings().map {
            _seekIncrement.value =
                SeekIncrementsState(it.seekForwardIncrementMs, it.seekBackIncrementMs)
            showMillisecondsInPositionText = it.shouldMillisecondsBeShown
            audioUpdateInterval = it.audioUpdateInterval.milliseconds
        }.launchIn(viewModelScope)
    }

    fun registerMediaListener() {
        browser.registerEventListener(mediaListener)
    }

    fun unregisterMediaListener() {
        browser.unregisterEventListener(mediaListener)
    }

    fun getTextForPosition(position: Long) =
        millisecondsToReadableString(position, showMillisecondsInPositionText)

    fun onSeekTo(position: Long) {
        seekPosition(position)
    }

    fun onItemClicked(playback: Playback?) {
        itemClicked(playback)
    }

    fun onSeekBack() {
        seekPosition(currentPosition - seekIncrement.value.backward)
    }

    fun onSeekForward() {
        seekPosition(currentPosition + seekIncrement.value.forward)
    }

    fun pauseResume() {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else if (!setCurrentPlaybackToRecentlyPlayed(playWhenReady = true))
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun nextRepeatMode() {
        _repeatMode.value = repeatMode.value.next
        browser.repeatMode = repeatMode.value

        viewModelScope.launch {
            _playbackState.value =
                playbackState.value.copy(
                    children = getNextPlaybackItems(
                        recentlyPlayed.value,
                        repeatMode.value
                    )
                )
        }
    }

    fun updateTimingData(i: Int, startTime: Long, endTime: Long) {
        val old = timingData.toList()
        old[i].startTime = startTime
        old[i].endTime = endTime

        timingData.clear()
        timingData.addAll(old)
    }

    fun setNewTimingData() {
        browser.timingData = TimingDataController(timingData, currentTimingDataIndex.value)
    }

    fun addNewTimingData() {
        timingData.add(TimingData(0L, playbackState.value.duration))
    }

    fun removeTimingData(i: Int) {
        timingData.removeAt(i)
    }

    // TODO: Make non-suspending
    suspend fun saveNewLoop(name: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext createAndSaveNewLoop(name) is Resource.Success
    }

    fun editPlaylist(i: Int, checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: Cast to SinglePlayback? might fail
            if (checked)
                savePlaybackToPlaylist(recentlyPlayed.value as SinglePlayback?, i)
            else
                removePlaybackFromPlaylist(recentlyPlayed.value as SinglePlayback?, i)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            createAndSaveNewPlaylist(name)
        }
    }


    private fun setCurrentPlaybackToRecentlyPlayed(
        playWhenReady: Boolean = false
    ): Boolean = setCurrentPlayback(recentlyPlayed.value, playWhenReady, recentlyPlayedPosition)

    private fun updatePlaybackState(playback: Playback?) {
        if (playback == null)
            return

        viewModelScope.launch {
            _playbackState.value = PlaybackState(
                title = playback.title ?: "Unknown Title",
                artist = playback.artist ?: "Unknown Artist",
                duration = playback.duration ?: 0L,
                children = getNextPlaybackItems(playback, repeatMode.value)
            )
        }
    }

    private fun updateArtworkState(playback: Playback?) {
        if (playback == null)
            return

        _artworkState.value =
            ArtworkState(artwork = playback.artwork, isArtworkLoading = playback.isArtworkLoading)
    }

    private fun getOrLoadArtworkForPlayback(playback: Playback) {
        viewModelScope.launch(Dispatchers.IO) {
            getOrLoadArtwork(playback.underlyingSong ?: return@launch).onEach {
                runOnUiThreadAsync {
                    recentlyPlayed.value?.artwork?.value = it.data?.artwork
                    recentlyPlayed.value?.isArtworkLoading?.value = false
                    updateArtworkState(recentlyPlayed.value)
                }
            }.collect()
        }
    }

    private inner class MediaListener : MediaBrowserController.EventListener {
        override fun onPlaybackTransition(playback: Playback?) {
            _recentlyPlayed.value = playback
            updatePlaybackState(playback)

            if (playback != null) {
                val newTimingData = playback.timingData
                if (newTimingData == null || newTimingData.timingData.isEmpty()) {
                    timingData.clear()
                    timingData.add(TimingData(0L, playback.duration!!))
                } else {
                    timingData.clear()
                    timingData.addAll(newTimingData.timingData)
                    _currentTimingDataIndex.value = newTimingData.currentIndex
                }

                getOrLoadArtworkForPlayback(playback)
            } else {
                timingData.clear()
                _currentTimingDataIndex.value = 0
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onTimingDataAdvanced(i: Int) {
            _currentTimingDataIndex.value = i
        }
    }
}