package com.tachyonmusic.presentation.player

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.player.CreateRemix
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.SaveRemixToDatabase
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.toPlayback
import com.tachyonmusic.presentation.util.update
import com.tachyonmusic.util.Config
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class RemixEditorViewModel @Inject constructor(
    private val mediaBrowser: MediaBrowserController,
    private val seekToPosition: SeekToPosition,
    private val createRemix: CreateRemix,
    private val playPlayback: PlayPlayback,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    settingsRepository: SettingsRepository,
    private val pauseResumePlayback: PauseResumePlayback,
    private val saveRemix: SaveRemixToDatabase,
    private val adInterface: AdInterface,
    private val dataRepository: DataRepository,
    private val remixRepository: RemixRepository
) : ViewModel() {
    private val _remixError = MutableStateFlow<UiText?>(null)
    val remixError = _remixError.asStateFlow()

    val duration = mediaBrowser.currentPlayback.map {
        it?.duration ?: Long.MAX_VALUE.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Long.MAX_VALUE.ms)

    val currentRemixName = mediaBrowser.currentPlayback.map {
        if (it?.isRemix == true) it.name!! else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val timingData = mutableStateListOf<TimingData>()
    var currentIndex by mutableIntStateOf(0)
        private set

    val settings = settingsRepository.observe()
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.WhileSubscribed(),
            SettingsEntity()
        )

    init {
        mediaBrowser.currentPlayback.onEach {
            val newTimingData = if (it == null)
                null
            else if (it.timingData.isNullOrEmpty())
                TimingDataController.default(it.duration)
            else
                it.timingData

            timingData.update { newTimingData?.timingData ?: emptyList() }
            currentIndex = newTimingData?.currentIndex ?: 0
        }.launchIn(viewModelScope)
    }

    fun setAndPlayTimingDataAt(i: Int, startFromEnd: Boolean = false) {
        if (startFromEnd)
            seekToPosition(timingData[i].endTime - Config.TIMING_DATA_END_TIME_ADJUSTMENT)
        else
            seekToPosition(timingData[i].startTime)
        pauseResumePlayback(PauseResumePlayback.Action.Resume)

        currentIndex = i
        setNewTimingData()
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
        mediaBrowser.updatePlayback {
            it?.copy(
                timingData = TimingDataController(timingData.toList(), currentIndex)
            )
        }
    }

    fun addNewTimingData(i: Int) {
        mediaBrowser.updatePlayback {
            it?.copy(
                timingData = TimingDataController(timingData.toMutableList().apply {
                    add(i, TimingData(0.ms, duration.value))
                }, currentIndex)
            )
        }
    }

    fun removeTimingData(i: Int) {
        mediaBrowser.updatePlayback {
            it?.copy(
                timingData = TimingDataController(timingData.toMutableList().apply {
                    removeAt(i)

                    // Add default back if last timingData was deleted
                    if (i == 0 && isEmpty())
                        add(TimingData(0.ms, duration.value))
                }, currentIndex)
            )
        }
    }

    fun moveTimingData(from: Int, to: Int) {
        mediaBrowser.updatePlayback {
            it?.copy(
                timingData = TimingDataController(timingData.toMutableList().apply {
                    if (isValidTimingDataMove(from, to)) {
                        val tdToMove = removeAt(from)
                        add(to, tdToMove)
                    }
                }, currentIndex)
            )
        }
    }

    fun saveNewRemix(name: String, activity: ComponentActivity?) {
        viewModelScope.launch {
            val sizeBefore = predefinedPlaylistsRepository.remixPlaylist.value.size
            val mediaPosBefore = mediaBrowser.currentPosition
            val currentPlayback = mediaBrowser.currentPlayback.value
            val createRes = createRemix(name, currentPlayback)

            if (createRes is Resource.Success) {
                _remixError.update { null }
                withContext(Dispatchers.IO) {
                    val dbRes = saveRemix(createRes.data!!, ignoreMaxRemixes = activity == null)

                    if (dbRes is Resource.Success && settings.value.playNewlyCreatedRemix) {
                        runOnUiThread {
                            pauseResumePlayback(PauseResumePlayback.Action.Pause)
                            // TODO: Some way to wait for predefined playlists repository to update
                            //  playPlayback is not working because new remix is not in predefined playlists yet
                            while (predefinedPlaylistsRepository.remixPlaylist.value.size <= sizeBefore) {
                                delay(1000.ms)
                            }

                            playPlayback(
                                createRes.data!!.toPlayback(currentPlayback!!),
                                mediaPosBefore
                            )
                        }
                    } else if (dbRes is Resource.Error && dbRes.code == SaveRemixToDatabase.ERROR_NEEDS_TO_SHOW_AD) {
                        runOnUiThread { pauseResumePlayback(PauseResumePlayback.Action.Pause) }
                        adInterface.showRewardAdSuspend(activity!!) { _, amount ->
                            val numStoredRemixes = dataRepository.getData().maxRemixCount
                            dataRepository.update(maxRemixCount = numStoredRemixes + amount)
                            saveNewRemix(name, activity)
                        }
                        runOnUiThread { pauseResumePlayback(PauseResumePlayback.Action.Resume) }
                    } else
                        _remixError.update { dbRes.message }
                }
            } else
                _remixError.update { createRes.message }
        }
    }

    fun isValidTimingDataMove(from: Int, to: Int): Boolean {
        if (timingData.size <= 1) return false
        if (timingData.size == to) return false
        if (from < 0 || to < 0) return false

        return true
    }

    // TODO: Optimize?
    fun requiresRemixCountIncrease(): Boolean {
        return runBlocking {
            val currentMax = dataRepository.getData().maxRemixCount
            val remixCount = remixRepository.getRemixes().size

            remixCount >= currentMax
        }
    }
}