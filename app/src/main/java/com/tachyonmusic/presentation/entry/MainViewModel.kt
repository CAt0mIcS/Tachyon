package com.tachyonmusic.presentation.entry

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.presentation.theme.ComposeSettings
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
    observeSettings: ObserveSettings,

    private val registerNewUriPermission: RegisterNewUriPermission,
    private val importDatabase: ImportDatabase
) : ViewModel() {

    private val _composeSettings = MutableStateFlow(ComposeSettings())
    val composeSettings = _composeSettings.asStateFlow()

    val requiresMusicPathSelection = observeSettings().map {
        it.musicDirectories.isEmpty()
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.WhileSubscribed(), true)

    fun setNewMusicDirectory(uri: Uri?) {
        viewModelScope.launch {
            registerNewUriPermission(uri)
        }
    }

    fun onImportDatabase(uri: Uri?) {
        viewModelScope.launch {
            importDatabase(uri)
        }
    }
}