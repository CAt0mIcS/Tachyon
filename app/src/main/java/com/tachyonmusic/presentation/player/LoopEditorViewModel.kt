package com.tachyonmusic.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.isNullOrEmpty
import com.tachyonmusic.domain.use_case.GetRepositoryStates
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewLoop
import com.tachyonmusic.domain.use_case.player.SetTimingData
import com.tachyonmusic.presentation.util.update
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.ms
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

    val timingData = mutableStateListOf<TimingData>()
    var currentIndex by mutableStateOf<Int?>(null)
        private set
    
    init {
        getRepositoryStates.playback().onEach {
            val newTimingData = if (it == null)
                null
            else if (it.timingData.isNullOrEmpty())
                TimingDataController(listOf(TimingData(0.ms, it.duration)))
            else
                it.timingData

            timingData.update { newTimingData?.timingData ?: emptyList() }
            currentIndex = newTimingData?.currentIndex
        }.launchIn(viewModelScope)
    }

    fun updateTimingData(i: Int, startTime: Duration, endTime: Duration) {
        timingData.update {
            it[i].startTime = startTime
            it[i].endTime = endTime
            it
        }
    }

    fun setNewTimingData() {
        setTimingData(
            TimingDataController(
                timingData,
                currentIndex ?: return
            )
        )
    }

    fun addNewTimingData(i: Int) {
        setTimingData(
            TimingDataController(
                timingData.toMutableList().apply {
                    add(i, TimingData(0.ms, duration.value))
                },
                currentIndex ?: 0
            )
        )
    }

    fun removeTimingData(i: Int) {
        setTimingData(
            TimingDataController(
                timingData.toMutableList().apply {
                    removeAt(i)
                },
                currentIndex ?: 0
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