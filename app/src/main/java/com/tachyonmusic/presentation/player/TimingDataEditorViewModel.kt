package com.tachyonmusic.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.player.CreateRemix
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.toRemix
import com.tachyonmusic.presentation.util.update
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import com.tachyonmusic.util.sec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class TimingDataEditorViewModel @Inject constructor(
    private val mediaBrowser: MediaBrowserController,
    private val seekToPosition: SeekToPosition,
    private val createRemix: CreateRemix,
    private val playPlayback: PlayPlayback,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    settingsRepository: SettingsRepository,
    private val pauseResumePlayback: PauseResumePlayback,
    private val remixRepository: RemixRepository
) : ViewModel() {
    private val _remixError = MutableStateFlow<UiText?>(null)
    val remixError = _remixError.asStateFlow()

    val duration = mediaBrowser.currentPlayback.map {
        it?.duration ?: Long.MAX_VALUE.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Long.MAX_VALUE.ms)

    val currentRemixName = mediaBrowser.currentPlayback.map {
        if(it is Remix) it.name else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val timingData = mutableStateListOf<TimingData>()
    var currentIndex by mutableIntStateOf(0)
        private set

    val settings = settingsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsEntity())

    init {
        mediaBrowser.currentPlayback.onEach {
            val newTimingData = if (it == null)
                null
            else if (it.timingData.isNullOrEmpty())
                TimingDataController(listOf(TimingData(0.ms, it.duration)))
            else
                it.timingData

            timingData.update { newTimingData?.timingData ?: emptyList() }
            currentIndex = newTimingData?.currentIndex ?: 0
        }.launchIn(viewModelScope)
    }

    fun playTimingDataAt(i: Int, startFromEnd: Boolean = false) {
        // TODO: Should be setting: If timing data is started from end we seek back this value to allow user to listen to the last few seconds
        val endTimeAdjustmentTime = 3.sec

        mediaBrowser.seekToTimingDataIndex(i)
        if (startFromEnd) {
            seekToPosition(timingData[i].endTime - endTimeAdjustmentTime)
        }
        currentIndex = i
        pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun updateTimingData(i: Int, startTime: Duration, endTime: Duration) {
        timingData.update {
            it[i].startTime = startTime
            it[i].endTime = endTime
            it
        }
    }

    fun updateStartToCurrentPosition(i: Int) {
        timingData.update {
            it[i].startTime = mediaBrowser.currentPosition ?: return@update it
            it
        }
    }

    fun updateEndToCurrentPosition(i: Int) {
        timingData.update {
            it[i].endTime = mediaBrowser.currentPosition ?: return@update it
            it
        }
    }

    fun setNewTimingData() {
        mediaBrowser.currentPlaybackTimingData =
            TimingDataController(timingData.toMutableList(), currentIndex)
    }

    fun addNewTimingData(i: Int) {
        mediaBrowser.currentPlaybackTimingData = TimingDataController(
            timingData.toMutableList().apply {
                add(i, TimingData(0.ms, duration.value))
            },
            currentIndex
        )
    }

    fun removeTimingData(i: Int) {
        mediaBrowser.currentPlaybackTimingData = TimingDataController(
            timingData.toMutableList().apply {
                removeAt(i)

                // Add default back if last timingData was deleted
                if(i == 0 && isEmpty())
                    add(TimingData(0.ms, duration.value))
            },
            currentIndex
        )
    }

    fun moveTimingData(from: Int, to: Int) {
        mediaBrowser.currentPlaybackTimingData = TimingDataController(
            timingData.toMutableList().apply {
                if(isValidTimingDataMove(from, to)) {
                    val tdToMove = removeAt(from)
                    add(to, tdToMove)
                }
            },
            currentIndex
        )
    }

    fun saveNewRemix(name: String) {
        viewModelScope.launch {
            val sizeBefore = predefinedPlaylistsRepository.remixPlaylist.value.size
            val mediaPosBefore = mediaBrowser.currentPosition
            val playback = mediaBrowser.currentPlayback.value
            val createRes = createRemix(name, playback, playback?.timingData)

            if (createRes is Resource.Success) {
                _remixError.update { null }
                withContext(Dispatchers.IO) {
                    val dbRes = remixRepository.add(createRes.data!!)

                    if (dbRes is Resource.Success) {
                        if (settings.value.playNewlyCreatedRemix) {
                            runOnUiThread {
                                pauseResumePlayback(PauseResumePlayback.Action.Pause)
                                // TODO: Some way to wait for predefined playlists repository to update
                                //  playPlayback is not working because new remix is not in predefined playlists yet
                                while (predefinedPlaylistsRepository.remixPlaylist.value.size <= sizeBefore) {
                                    delay(50.ms)
                                }

                                playPlayback(
                                    createRes.data!!.toRemix(playback?.underlyingSong),
                                    mediaPosBefore
                                )
                            }
                        }
                    } else
                        _remixError.update { dbRes.message }
                }
            } else
                _remixError.update { createRes.message }
        }
    }


    fun isValidTimingDataMove(from: Int, to: Int): Boolean {
        if(timingData.size <= 1) return false
        if(timingData.size == to) return false
        if(from < 0 || to < 0) return false

        return true
    }
}