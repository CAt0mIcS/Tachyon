package com.tachyonmusic.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.presentation.BottomNavigationItem
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.profile.component.CreateFileDialog
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
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
        var showCreateFileDialog by remember { mutableStateOf(false) }
        var showSelectFileDialog by remember { mutableStateOf(false) }

        UriPermissionDialog(showUriPermissionDialog) {
            viewModel.onUriPermissionResult(it)
            showUriPermissionDialog = false
        }

        CreateFileDialog(
            showCreateFileDialog,
            Database.JSON_MIME_TYPE,
            Database.BACKUP_FILE_NAME
        ) {
            viewModel.onExportDatabase(it)
            showCreateFileDialog = false
        }

        OpenDocumentDialog(showSelectFileDialog, Database.JSON_MIME_TYPE) {
            viewModel.onImportDatabase(it)
            showSelectFileDialog = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Theme.padding.medium)
                .verticalScroll(rememberScrollState()),
        ) {

            val settings by viewModel.settings.collectAsState()

            val headlineFontSize = 26.sp
            val headlineFontWeight = FontWeight.Bold

            /**
             * General
             */
            Text("General", fontSize = headlineFontSize, fontWeight = headlineFontWeight)
            Setting(
                text = "Show Milliseconds",
                desc = "Whether milliseconds should be shown in the Player"
            ) {
                Switch(
                    checked = settings.shouldMillisecondsBeShown,
                    onCheckedChange = viewModel::shouldMillisecondsBeShownChanged
                )
            }

            Setting(
                text = "Dynamic Colors",
                desc = "If enabled the app's color theme will be determined by your Android background image"
            ) {
                Switch(
                    checked = settings.dynamicColors,
                    onCheckedChange = viewModel::dynamicColorsChanged
                )
            }

            Setting(
                text = "Album Artwork Fetcher",
                desc = "If enabled the app will try to find matching artwork to any songs that do not have any embedded in its .mp3 file"
            ) {
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

            Setting(text = "Animations") {
                Switch(
                    checked = settings.animateText,
                    onCheckedChange = viewModel::onAnimateTextChanged
                )
            }


            /**
             * Playback
             */
            HorizontalDivider(modifier = Modifier.padding(vertical = Theme.padding.medium))
            Text("Playback", fontSize = headlineFontSize, fontWeight = headlineFontWeight)
            Setting(
                text = "Combined Playback Playlist",
                desc = "If enabled and a song is started clicking next until after the last song will switch to the list of customized songs, if a customized song is started clicking next until after the last customizes song will switch to the list of songs"
            ) {
                Switch(
                    checked = settings.combineDifferentPlaybackTypes,
                    onCheckedChange = viewModel::combineDifferentPlaybackTypesChanged
                )
            }

            Setting(
                text = "Audio Mixing",
                desc = "If enabled, the media playing in the app won't stop if another app starts playing audio. This allows you to e.g. play a song in the app and switch to a YouTube video, without the song pausing automatically"
            ) {
                Switch(
                    checked = settings.ignoreAudioFocus,
                    onCheckedChange = viewModel::ignoreAudioFocusChanged
                )
            }

            Setting(
                text = "Seek Forward Increment",
                desc = "Time in seconds to seek forward when the seek forward button is pressed"
            ) {
                TextField(
                    value = settings.seekForwardIncrement.inWholeSeconds.toString(),
                    onValueChange = {
                        viewModel.seekForwardIncrementChanged(
                            it.toLongOrNull()?.sec ?: return@TextField
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
            }

            Setting(
                text = "Seek Back Increment",
                desc = "Time in seconds to seek back when the seek back button is pressed"
            ) {
                TextField(
                    value = settings.seekBackIncrement.inWholeSeconds.toString(),
                    onValueChange = {
                        viewModel.seekBackIncrementChanged(
                            it.toLongOrNull()?.sec ?: return@TextField
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
            }

            Setting(
                text = "Play Newly Created",
                desc = "If enabled a newly created customized song will automatically start playing after creation"
            ) {
                Switch(
                    checked = settings.playNewlyCreatedCustomizedSong,
                    onCheckedChange = viewModel::playNewlyCreatedCustomizedSong
                )
            }


            /**
             * Database
             */
            HorizontalDivider(modifier = Modifier.padding(vertical = Theme.padding.medium))
            Text("Database", fontSize = headlineFontSize, fontWeight = headlineFontWeight)
            Setting(
                textComposable = {
                    Button(onClick = { showUriPermissionDialog = true }) {
                        Text("Add Music Directory")
                    }
                },
                desc = "Allows you to select a new music directory to fetch songs from"
            ) {}

            Setting(
                textComposable = {
                    Button(onClick = { showCreateFileDialog = true }) {
                        Text("Export Database")
                    }
                }
            ) {}

            Setting(
                textComposable = {
                    Button(onClick = { showSelectFileDialog = true }) {
                        Text("Import Database")
                    }
                }
            ) {}


            /**
             * Advanced
             */
            HorizontalDivider(modifier = Modifier.padding(vertical = Theme.padding.medium))
            Text("Advanced", fontSize = headlineFontSize, fontWeight = headlineFontWeight)
            Setting(
                text = "History Playback Count",
                desc = "Maximum number of playbacks stored in history. Increasing this will lead to higher storage usage"
            ) {
                TextField(
                    value = settings.maxPlaybacksInHistory.toString(),
                    onValueChange = {
                        viewModel.maxPlaybacksInHistoryChanged(it.toIntOrNull() ?: return@TextField)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
            }

            Setting(
                text = "Audio Update Interval",
                desc = "Interval in milliseconds when the audio is updated (e.g. checked if loop has reached the end)"
            ) {
                TextField(
                    value = settings.audioUpdateInterval.inWholeMilliseconds.toString(),
                    onValueChange = {
                        viewModel.audioUpdateIntervalChanged(
                            it.toLongOrNull()?.ms ?: return@TextField
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
private fun Setting(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    desc: String? = null,
    content: @Composable () -> Unit
) {
    Setting({
        Text(text, Modifier.align(Alignment.CenterVertically))
    }, modifier, enabled, desc, content)
}


@Composable
fun Setting(
    textComposable: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    desc: String? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .padding(start = Theme.padding.medium)
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (enabled) 1f else .6f
            },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        ) {
            textComposable()

            if (desc != null) {
                var showDescriptivePopup by remember { mutableStateOf(false) }

                DropdownMenu(
                    modifier = Modifier.padding(Theme.padding.medium),
                    expanded = showDescriptivePopup,
                    onDismissRequest = { showDescriptivePopup = false },
                    content = {
                        Text(desc)
                    }
                )

                IconButton(
                    onClick = { showDescriptivePopup = !showDescriptivePopup },
                    modifier = Modifier
                        .padding(Theme.padding.small)
                        .size(18.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.question_mark),
                        contentDescription = "Setting description"
                    )
                }
            }
        }

        Box(modifier = Modifier.width(56.dp)) {
            content()
        }
    }
}
