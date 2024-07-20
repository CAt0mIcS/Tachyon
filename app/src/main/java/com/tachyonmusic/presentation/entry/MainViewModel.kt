package com.tachyonmusic.presentation.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.data.repository.StateRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.repository.STATE_LOADING_TASK_STARTUP
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.home.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.theme.ComposeSettings
import com.tachyonmusic.util.sec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val stateRepository: StateRepository,

    settingsRepository: SettingsRepository,
    updateSettingsDatabase: UpdateSettingsDatabase,
    updateSongDatabase: UpdateSongDatabase,

    private val registerNewUriPermission: RegisterNewUriPermission,
    private val importDatabase: ImportDatabase,

    private val log: Logger
) : ViewModel() {

    val isLoading = stateRepository.isLoading

    private val _composeSettings = MutableStateFlow(ComposeSettings())
    val composeSettings = _composeSettings.asStateFlow()

    private val _requiredMusicDirectoriesAfterDatabaseImport = MutableStateFlow(emptyList<String>())
    val requiredMusicDirectoriesAfterDatabaseImport =
        _requiredMusicDirectoriesAfterDatabaseImport.asStateFlow()

    private var cachedMusicDirectories = emptyList<Uri>()

    val requiresMusicPathSelection = combine(
        settingsRepository.observe(),
        requiredMusicDirectoriesAfterDatabaseImport
    ) { settings, requiredPathsAfterImport ->
        settings.musicDirectories.isEmpty() || requiredPathsAfterImport.isNotEmpty()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            cachedMusicDirectories = settingsRepository.getSettings().musicDirectories
            updateSettingsDatabase()

            settingsRepository.observe().onEach { settings ->
                val loadingTaskRunning =
                    stateRepository.isLoadingTaskRunning(STATE_LOADING_TASK_STARTUP)
                if (loadingTaskRunning ||
                    (settings.musicDirectories.isNotEmpty() && cachedMusicDirectories != settings.musicDirectories)
                ) {
                    log.info("Starting song database update due to new music directory or reload")
                    updateSongDatabase(settings)
                    cachedMusicDirectories = settings.musicDirectories

                    if (loadingTaskRunning)
                        stateRepository.finishLoadingTask(STATE_LOADING_TASK_STARTUP)
                }

                _composeSettings.update { ComposeSettings(settings.animateText, settings.dynamicColors, settings.audioUpdateInterval) }
            }.collect()
        }
    }

    fun setNewMusicDirectory(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("MainViewModel::registerNewUriPermission")
            if (registerNewUriPermission(uri)) { // TODO: Test if updated correctly
                _requiredMusicDirectoriesAfterDatabaseImport.update {
                    it.toMutableList().apply { removeAll { path -> uri!!.encodedPath == path } }
                }
            }
            stateRepository.finishLoadingTask(
                "MainViewModel::registerNewUriPermission",
                timeout = .5.sec
            )
        }
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("MainViewModel::importDatabase")
            val missingUri = importDatabase(uri)

            // TODO: Handle null case for missingUri ^ and it.path >
            if (missingUri != null) {
                _requiredMusicDirectoriesAfterDatabaseImport.update { missingUri.mapNotNull { it.encodedPath } }
            }

            stateRepository.finishLoadingTask(
                "MainViewModel::importDatabase",
                timeout = .5.sec
            )
        }
    }
}