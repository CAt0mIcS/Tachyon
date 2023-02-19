package com.tachyonmusic.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.GetCurrentPlaybackState
import com.tachyonmusic.domain.use_case.player.GetTimingDataState
import com.tachyonmusic.domain.use_case.player.SetNewTimingData
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimingDataSeek(
    val i: Int,
    val start: Duration,
    val end: Duration
)

@HiltViewModel
class LoopEditorViewModel @Inject constructor(
    getTimingDataState: GetTimingDataState,
    getPlaybackState: GetCurrentPlaybackState,
    private val setBrowserTimingData: SetNewTimingData,
    private val createAndSaveNewLoop: CreateAndSaveNewLoop,
) : ViewModel() {
    private val _loopError = MutableStateFlow<UiText?>(null)
    val loopError = _loopError.asStateFlow()

    val duration = getPlaybackState().map {
        it?.duration ?: 0.ms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.ms)

    private val _timingDataSeek = MutableStateFlow<TimingDataSeek?>(null)
    val timingDataSeek = _timingDataSeek.asStateFlow()

    val timingData =
        combine(getTimingDataState(), duration, timingDataSeek) { timingData, duration, seekInfo ->
            var current = timingData ?: TimingDataController(listOf(TimingData(0.ms, duration)))

            if(seekInfo != null) {
                current = TimingDataController(current.timingData.apply {
                    this[seekInfo.i].startTime = seekInfo.start
                    this[seekInfo.i].endTime = seekInfo.end
                }, current.currentIndex)
            }

            println("TD: $current, $seekInfo")
            current
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TimingDataController())

    fun updateTimingData(i: Int, start: Duration, end: Duration) {
        _timingDataSeek.update { TimingDataSeek(i, start, end) }
    }

    fun setNewTimingData() {
        setBrowserTimingData(timingData.value)
        _timingDataSeek.update { null }
    }

    fun addNewTimingData(i: Int) {
        val new = TimingDataController(timingData.value.timingData.apply {
            add(i, TimingData(0.ms, duration.value))
        }, timingData.value.currentIndex)

        setBrowserTimingData(new)
    }

    fun removeTimingData(i: Int) {
        val new = TimingDataController(timingData.value.timingData.apply {
            removeAt(i)
        }, timingData.value.currentIndex)

        setBrowserTimingData(new)
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