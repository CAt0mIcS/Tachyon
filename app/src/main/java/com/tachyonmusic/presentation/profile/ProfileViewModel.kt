package com.tachyonmusic.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.util.Duration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun shouldMillisecondsBeShownChanged(shouldShow: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            writeSettings(shouldMillisecondsBeShown = shouldShow)
        }
    }
}