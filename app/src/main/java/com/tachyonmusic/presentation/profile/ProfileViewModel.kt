package com.tachyonmusic.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tachyonmusic.util.Duration

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeSettings: ObserveSettings,
    private val writeSettings: WriteSettings
) : ViewModel() {

    private var _settings = mutableStateOf(SettingsEntity())
    val settings: State<SettingsEntity> = _settings


    init {
        observeSettings().onEach {
            _settings.value = it
        }.launchIn(viewModelScope)
    }


    fun seekForwardIncrementChanged(inc: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(seekForwardIncrement = inc))
        }
    }

    fun seekBackIncrementChanged(inc: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(seekBackIncrement = inc))
        }
    }

    fun ignoreAudioFocusChanged(ignore: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(ignoreAudioFocus = ignore))
        }
    }

    fun autoDownloadAlbumArtworkChanged(autoDownload: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(autoDownloadAlbumArtwork = autoDownload))
        }
    }

    fun autoDownloadAlbumArtworkWifiOnly(downloadWifiOnly: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(autoDownloadAlbumArtworkWifiOnly = downloadWifiOnly))
        }
    }

    fun combineDifferentPlaybackTypesChanged(combine: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(combineDifferentPlaybackTypes = combine))
        }
    }

    fun maxPlaybacksInHistoryChanged(maxPlaybacks: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(maxPlaybacksInHistory = maxPlaybacks))
        }
    }

    fun audioUpdateIntervalChanged(interval: Duration) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(audioUpdateInterval = interval))
        }
    }

    fun shouldMillisecondsBeShownChanged(shouldShow: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(settings.value.copy(shouldMillisecondsBeShown = shouldShow))
        }
    }
}