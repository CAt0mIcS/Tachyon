package com.tachyonmusic.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.profile.ExportDatabase
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.util.Duration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeSettings: ObserveSettings,
    private val writeSettings: WriteSettings,
    private val registerNewUriPermission: RegisterNewUriPermission,
    private val exportDatabase: ExportDatabase,
    private val importDatabase: ImportDatabase
) : ViewModel() {

    val settings = observeSettings().stateIn(
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
            registerNewUriPermission(uri)
        }
    }

    fun onExportDatabase(uri: Uri?) {
        exportDatabase(uri)
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch {
            importDatabase(uri)
        }
    }
}