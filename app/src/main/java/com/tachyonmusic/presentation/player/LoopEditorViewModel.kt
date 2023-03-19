package com.tachyonmusic.presentation.player

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.SetTimingData
import com.tachyonmusic.presentation.util.update
import com.tachyonmusic.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoopEditorViewModel @Inject constructor(
    getRepositoryStates: GetRepositoryStates,
    private val setTimingData: SetTimingData,
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
) : ViewModel() {
    private val _loopError = MutableStateFlow<UiText?>(null)
    val loopError = _loopError.asStateFlow()

    val duration = getRepositoryStates.playback().map {
        it?.duration ?: Long.MAX_VALUE.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Long.MAX_VALUE.ms)

    val seekTimingData = mutableStateListOf<TimingData>()
    val timingData = getRepositoryStates.playback().map { playback ->
        println("NEWTD: Setting index of $playback to: ${playback?.timingData?.currentIndex}")

        val newTimingData = if (playback == null)
            null
        else if (playback.timingData.isNullOrEmpty())
            TimingDataController(listOf(TimingData(0.ms, playback.duration)))
        else
            playback.timingData

        seekTimingData.update { newTimingData?.timingData ?: emptyList() }

        newTimingData
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // TODO: When adding new timing data while playing loop index sometimes won't be updated anymore

    fun updateTimingData(i: Int, startTime: Duration, endTime: Duration) {
        seekTimingData.update {
            it[i].startTime = startTime
            it[i].endTime = endTime
            it
        }
    }

    fun setNewTimingData() {
        setTimingData(
            TimingDataController(
                seekTimingData.copy(),
                timingData.value?.currentIndex ?: return
            )
        )
    }

    fun addNewTimingData(i: Int) {
        setTimingData(
            timingData.value?.copy(
                timingData = timingData.value?.timingData?.toMutableList()?.apply {
                    add(i, TimingData(0.ms, duration.value))
                } ?: return
            )
        )
    }

    fun removeTimingData(i: Int) {
        setTimingData(
            timingData.value?.copy(
                timingData = timingData.value?.timingData?.toMutableList()?.apply {
                    removeAt(i)
                } ?: return
            )
        )
    }

    fun saveNewLoop(name: String) {
        viewModelScope.launch {
            val res = createAndSaveNewLoop(name)
            if (res is Resource.Success)
                _loopError.value = null
            else
                _loopError.value = res.message
        }
    }
}