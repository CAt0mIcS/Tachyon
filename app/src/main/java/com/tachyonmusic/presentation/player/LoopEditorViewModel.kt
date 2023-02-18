package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.GetCurrentPlaybackState
import com.tachyonmusic.domain.use_case.player.SetNewTimingData
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoopEditorViewModel @Inject constructor(
    getPlaybackState: GetCurrentPlaybackState,
    private val setBrowserTimingData: SetNewTimingData,
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
) : ViewModel() {
    private val _timingData = MutableStateFlow(emptyList<TimingData>())
    val timingData = _timingData.asStateFlow()

    private val _currentTimingDataIndex = MutableStateFlow(0)
    val currentTimingDataIndex = _currentTimingDataIndex.asStateFlow()

    private val _loopError = MutableStateFlow<UiText?>(null)
    val loopError = _loopError.asStateFlow()

    val duration = getPlaybackState().map { 
        it?.duration ?: 0.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.ms)

    fun updateTimingData(i: Int, start: Duration, end: Duration) {
        _timingData.update {
            it[i].startTime = start
            it[i].endTime = end
            it
        }
    }

    fun setNewTimingData() {
        setBrowserTimingData(TimingDataController(timingData.value, currentTimingDataIndex.value))
    }

    fun addNewTimingData(i: Int) {
        _timingData.update {
            it.toMutableList().apply {
                add(i, TimingData(0.ms, duration.value))
            }
        }
    }

    fun removeTimingData(i: Int) {
        _timingData.update {
            it.toMutableList().apply {
                removeAt(i)
            }
        }
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