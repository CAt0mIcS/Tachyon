package com.tachyonmusic.presentation.player

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.domain.use_case.GetMediaStates
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.SetNewTimingData
import com.tachyonmusic.presentation.util.update
import com.tachyonmusic.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoopEditorViewModel @Inject constructor(
    getMediaStates: GetMediaStates,
    private val setBrowserTimingData: SetNewTimingData,
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
) : ViewModel() {
    private val _loopError = MutableStateFlow<UiText?>(null)
    val loopError = _loopError.asStateFlow()

    val duration = getMediaStates.playback().map {
        it?.duration ?: Long.MAX_VALUE.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Long.MAX_VALUE.ms)

    private val _timingData = mutableStateListOf<TimingData>()
    val timingData: List<TimingData> = _timingData

    // TODO: When adding new timing data while playing loop index sometimes won't be updated anymore
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    init {
        combine(getMediaStates.timingData(), duration) { timingData, duration ->
            val newTimingData = if (timingData.isNullOrEmpty())
                listOf(TimingData(0.ms, duration))
            else
                timingData.timingData

            _timingData.update { newTimingData }
            _currentIndex.update { timingData?.currentIndex ?: 0 }
        }.launchIn(viewModelScope)
    }

    fun updateTimingData(i: Int, start: Duration, end: Duration) {
        _timingData.update {
            it[i].startTime = start
            it[i].endTime = end
            it
        }
    }

    fun setNewTimingData() {
        setBrowserTimingData(TimingDataController(timingData.toList(), currentIndex.value))
    }

    fun addNewTimingData(i: Int) {
        setBrowserTimingData(
            TimingDataController(
                _timingData.apply {
                    add(i, TimingData(0.ms, duration.value))
                }.toList(),
                currentIndex.value
            )
        )
    }

    fun removeTimingData(i: Int) {
        setBrowserTimingData(
            TimingDataController(
                _timingData.apply {
                    removeAt(i)
                }.toList(),
                currentIndex.value
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