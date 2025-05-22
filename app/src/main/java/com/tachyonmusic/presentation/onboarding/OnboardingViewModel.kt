package com.tachyonmusic.presentation.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.StateRepository
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import com.tachyonmusic.util.sec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val uriPermissionRepository: UriPermissionRepository,
    private val dataRepository: DataRepository,
    private val stateRepository: StateRepository,
    private val registerNewUriPermission: RegisterNewUriPermission,
    private val importDatabase: ImportDatabase
) : ViewModel() {

    // Placeholder so that the screen doesn't advance when relaunching app during setup
    // Removed in the ViewModel's init block after loading required paths
    private val _requiredMusicDirectoriesAfterDatabaseImport = MutableStateFlow(listOf(""))
    val requiredMusicDirectoriesAfterDatabaseImport =
        _requiredMusicDirectoriesAfterDatabaseImport.asStateFlow()

    val musicDirectorySelected = combine(
        settingsRepository.observe(),
        requiredMusicDirectoriesAfterDatabaseImport
    ) { settings, requiredDirs ->
        settings.musicDirectories.isNotEmpty() && requiredDirs.isEmpty()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val requiredDirectories = settingsRepository.getSettings().musicDirectories
            val directoriesToAskForPermission = mutableListOf<String?>()
            for (requiredDir in requiredDirectories) {
                if (!uriPermissionRepository.hasPermission(requiredDir))
                    directoriesToAskForPermission.add(requiredDir.encodedPath)
            }

            _requiredMusicDirectoriesAfterDatabaseImport.update {
                directoriesToAskForPermission.filterNotNull()
            }
        }
    }

    fun saveOnboardingState(completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.update(onboardingCompleted = completed)
        }
    }

    fun setNewMusicDirectory(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("OnboardingViewModel::registerNewUriPermission")
            if (registerNewUriPermission(uri)) { // TODO: Test if updated correctly
                _requiredMusicDirectoriesAfterDatabaseImport.update {
                    it.toMutableList().apply { removeAll { path -> uri!!.encodedPath == path } }
                }
            }
            stateRepository.finishLoadingTask(
                "OnboardingViewModel::registerNewUriPermission",
                timeout = .5.sec
            )
        }
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("OnboardingViewModel::importDatabase")
            // Place dummy item in here so that [readyToAdvance] doesn't become true for
            // some milliseconds after [importDatabase] is run or after relaunching the app during setup
            _requiredMusicDirectoriesAfterDatabaseImport.update { listOf("") }
            val missingUris = importDatabase(uri)

            // TODO: Handle null case for missingUri ^ and it.path >
            if (missingUris != null) {
                _requiredMusicDirectoriesAfterDatabaseImport.update { missingUris.mapNotNull { it.encodedPath } }
            } else {
                _requiredMusicDirectoriesAfterDatabaseImport.update { emptyList() }
            }

            stateRepository.finishLoadingTask(
                "OnboardingViewModel::importDatabase",
                timeout = .5.sec
            )
        }
    }
}