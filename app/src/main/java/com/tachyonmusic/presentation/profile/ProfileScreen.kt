package com.tachyonmusic.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.sec

object ProfileScreen :
    BottomNavigationItem(R.string.btmNav_profile, R.drawable.ic_profile, "profile") {

    @Composable
    operator fun invoke(
        viewModel: ProfileViewModel = hiltViewModel()
    ) {
        var showUriPermissionDialog by remember { mutableStateOf(false) }
        UriPermissionDialog(showUriPermissionDialog) {
            viewModel.onUriPermissionResult(it)
            showUriPermissionDialog = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Theme.padding.medium)
                .verticalScroll(rememberScrollState()),
        ) {

            val settings by viewModel.settings.collectAsState()

            Setting(text = "Enable background audio mixing") {
                Switch(
                    checked = settings.ignoreAudioFocus,
                    onCheckedChange = viewModel::ignoreAudioFocusChanged
                )
            }


            Setting(text = "Automatically download album artwork") {
                Switch(
                    checked = settings.autoDownloadAlbumArtwork,
                    onCheckedChange = viewModel::autoDownloadAlbumArtworkChanged
                )
            }

            Setting(
                text = "WIFI-Only",
                enabled = settings.autoDownloadAlbumArtwork,
                modifier = Modifier.padding(start = Theme.padding.large)
            ) {
                Switch(
                    checked = settings.autoDownloadAlbumArtworkWifiOnly,
                    onCheckedChange = viewModel::autoDownloadAlbumArtworkWifiOnly
                )
            }

            Setting(text = "Combine songs and loops in playlist") {
                Switch(
                    checked = settings.combineDifferentPlaybackTypes,
                    onCheckedChange = viewModel::combineDifferentPlaybackTypesChanged
                )
            }

            Setting(text = "Increment when seeking forward") {
                TextField(
                    value = settings.seekForwardIncrement.inWholeSeconds.toString(),
                    onValueChange = {
                        viewModel.seekForwardIncrementChanged(it.toLong().sec)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            Setting(text = "Increment when seeking back") {
                TextField(
                    value = settings.seekBackIncrement.inWholeSeconds.toString(),
                    onValueChange = {
                        viewModel.seekBackIncrementChanged(it.toLong().sec)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            Setting(text = "Animate text") {
                Switch(
                    checked = settings.animateText,
                    onCheckedChange = viewModel::onAnimateTextChanged
                )
            }

            Setting(text = "Maximum number of playbacks stored in history") {
                TextField(
                    value = settings.maxPlaybacksInHistory.toString(),
                    onValueChange = {
                        viewModel.maxPlaybacksInHistoryChanged(it.toInt())
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            Setting(text = "Interval in milliseconds when the audio state is updated") {
                TextField(
                    value = settings.audioUpdateInterval.inWholeMilliseconds.toString(),
                    onValueChange = {
                        viewModel.audioUpdateIntervalChanged(it.toLong().ms)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            Setting(text = "Whether milliseconds should be shown in time texts") {
                Switch(
                    checked = settings.shouldMillisecondsBeShown,
                    onCheckedChange = viewModel::shouldMillisecondsBeShownChanged
                )
            }


            Setting(text = "Add new music directory") {
                Button(onClick = { showUriPermissionDialog = true }) {
                    Text("Select")
                }
            }
        }
    }
}


@Composable
private fun Setting(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (enabled) 1f else .6f
            },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, modifier = Modifier.weight(1f))
        content()
    }
}