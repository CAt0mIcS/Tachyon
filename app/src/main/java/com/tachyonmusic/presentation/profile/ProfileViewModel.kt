package com.tachyonmusic.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.data.repository.StateRepository
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.profile.ExportDatabase
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.sec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val writeSettings: WriteSettings,
    private val registerNewUriPermission: RegisterNewUriPermission,
    private val stateRepository: StateRepository,
    private val exportDatabase: ExportDatabase,
    private val importDatabase: ImportDatabase,
    private val updateSongDatabase: UpdateSongDatabase,
) : ViewModel() {

    val settings = settingsRepository.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SettingsEntity()
    )


    fun seekForwardIncrementChanged(inc: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(seekForwardIncrement = inc)
        }
    }

    fun seekBackIncrementChanged(inc: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(seekBackIncrement = inc)
        }
    }

    fun onAnimateTextChanged(animateText: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(animateText = animateText)
        }
    }

    fun ignoreAudioFocusChanged(ignore: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(ignoreAudioFocus = ignore)
        }
        // TODO: Audio in Android Auto won't play if audio focus is not requested
    }

    fun autoDownloadAlbumArtworkChanged(autoDownload: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(autoDownloadAlbumArtwork = autoDownload)
        }
    }

    fun autoDownloadAlbumArtworkWifiOnly(downloadWifiOnly: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(autoDownloadAlbumArtworkWifiOnly = downloadWifiOnly)
        }
    }

    fun combineDifferentPlaybackTypesChanged(combine: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(combineDifferentPlaybackTypes = combine)
        }
    }

    fun maxPlaybacksInHistoryChanged(maxPlaybacks: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(maxPlaybacksInHistory = maxPlaybacks)
        }
    }

    fun audioUpdateIntervalChanged(interval: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(audioUpdateInterval = interval)
        }
    }

    fun playNewlyCreatedCustomizedSong(playNewly: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(playNewlyCreatedCustomizedSong = playNewly)
        }
    }

    fun shouldMillisecondsBeShownChanged(shouldShow: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(shouldMillisecondsBeShown = shouldShow)
        }
    }

    fun onUriPermissionResult(uri: Uri?) {
        viewModelScope.launch {
            stateRepository.queueLoadingTask("ProfileViewModel::registerNewUriPermission")
            registerNewUriPermission(uri)
            stateRepository.finishLoadingTask(
                "ProfileViewModel::registerNewUriPermission",
                timeout = .5.sec
            )
        }
    }

    fun onExportDatabase(uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            stateRepository.queueLoadingTask("ProfileViewModel::exportDatabase")
            exportDatabase(uri)
            stateRepository.finishLoadingTask("ProfileViewModel::exportDatabase")
        }
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            stateRepository.queueLoadingTask("ProfileViewModel::importDatabase")
            if (importDatabase(uri) != null) { // TODO: Display required music paths
                updateSongDatabase(settingsRepository.getSettings())
            }
            stateRepository.finishLoadingTask("ProfileViewModel::importDatabase")
        }
    }
}