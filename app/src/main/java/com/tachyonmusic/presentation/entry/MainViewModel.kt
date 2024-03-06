package com.tachyonmusic.presentation.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.data.repository.StateRepository
import com.tachyonmusic.domain.repository.STATE_LOADING_TASK_STARTUP
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.presentation.theme.ComposeSettings
import com.tachyonmusic.util.sec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val stateRepository: StateRepository,
    observeSettings: ObserveSettings,

    private val registerNewUriPermission: RegisterNewUriPermission,
    private val importDatabase: ImportDatabase
) : ViewModel() {

    val isLoading = stateRepository.isLoading

    private val _composeSettings = MutableStateFlow(ComposeSettings())
    val composeSettings = _composeSettings.asStateFlow()

    val requiresMusicPathSelection = observeSettings().map {
        it.musicDirectories.isEmpty()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), true)

    init {
        if (requiresMusicPathSelection.value)
            stateRepository.finishLoadingTask(STATE_LOADING_TASK_STARTUP)
    }

    fun setNewMusicDirectory(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("MainViewModel::registerNewUriPermission")
            registerNewUriPermission(uri)
            stateRepository.finishLoadingTask(
                "MainViewModel::registerNewUriPermission",
                timeout = .5.sec
            )
        }
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("MainViewModel::importDatabase")
            importDatabase(uri)
            stateRepository.finishLoadingTask(
                "MainViewModel::importDatabase",
                timeout = .5.sec
            )
        }
    }
}