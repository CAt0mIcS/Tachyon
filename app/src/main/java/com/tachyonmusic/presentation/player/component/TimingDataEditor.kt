package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.excludeFromSystemGesture
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.core_components.ErrorDialog
import com.tachyonmusic.presentation.player.TimingDataEditorViewModel
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.asString
import com.tachyonmusic.presentation.util.isEnabled
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.toReadableString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimingDataEditor(
    modifier: Modifier = Modifier,
    viewModel: TimingDataEditorViewModel = hiltViewModel()
) {
    val timingData = viewModel.timingData
    val error by viewModel.customizedSongError.collectAsState()

    if (error != null)
        ErrorDialog(title = stringResource(R.string.warning), subtitle = error.asString())

    Column(modifier = modifier) {
        val duration by viewModel.duration.collectAsState()

        for (i in timingData.indices) {

            val sliderColor =
                if (i == viewModel.currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        Theme.padding.extremelySmall,
                        MaterialTheme.colorScheme.primary,
                        Theme.shapes.medium
                    )
                    .padding(Theme.padding.medium)

            ) {
                val settings by viewModel.settings.collectAsState()
                val deltaDuration = settings.audioUpdateInterval

                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Button(
                            onClick = {
                                viewModel.updateTimingData(
                                    i,
                                    timingData[i].startTime - deltaDuration,
                                    timingData[i].endTime
                                )
                                viewModel.setNewTimingData()
                                viewModel.playTimingDataAt(i)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "<")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.updateTimingData(
                                    i,
                                    timingData[i].startTime + deltaDuration,
                                    timingData[i].endTime
                                )
                                viewModel.setNewTimingData()
                                viewModel.playTimingDataAt(i)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = ">")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.updateStartToCurrentPosition(i)
                                viewModel.setNewTimingData()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("<>")
                        }
                    }


                    item {
                        IconButton(
                            onClick = { viewModel.playTimingDataAt(i) },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_play),
                                contentDescription = "Seek to timing data"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(timingData[i].startTime.toReadableString(true))
                    Text(timingData[i].endTime.toReadableString(true))
                }

                RangeSlider(
                    modifier = Modifier
                        .systemGestureExclusion()
                        .align(Alignment.CenterHorizontally),
                    value = timingData[i].startTime.inWholeMilliseconds.toFloat()..timingData[i].endTime.inWholeMilliseconds.toFloat(),
                    onValueChange = {
                        viewModel.updateTimingData(i, it.start.ms, it.endInclusive.ms)
                    },
                    onValueChangeFinished = viewModel::setNewTimingData,
                    valueRange = 0f..duration.inWholeMilliseconds.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = sliderColor,
                        activeTrackColor = sliderColor,
                        inactiveTrackColor = if (i == viewModel.currentIndex)
                            MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSecondary
                    )
                )

                LazyRow(modifier = Modifier.align(Alignment.End)) {
                    item {
                        var timingDataInfoDropdownExpanded by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                timingDataInfoDropdownExpanded = !timingDataInfoDropdownExpanded
                            }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                "Additional Loop options for timing data $i"
                            )
                        }

                        DropdownMenu(
                            expanded = timingDataInfoDropdownExpanded,
                            onDismissRequest = { timingDataInfoDropdownExpanded = false }
                        ) {
                            var isMoveUpValid by remember { mutableStateOf(false) }
                            var isMoveDownValid by remember { mutableStateOf(false) }
                            LaunchedEffect(timingData) {
                                isMoveUpValid = viewModel.isValidTimingDataMove(i, i - 1)
                                isMoveDownValid = viewModel.isValidTimingDataMove(i, i + 1)
                            }

                            DropdownMenuItem(
                                text = { Text("Insert before") },
                                onClick = {
                                    viewModel.addNewTimingData(i)
                                    timingDataInfoDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Insert after") },
                                onClick = {
                                    viewModel.addNewTimingData(i + 1)
                                    timingDataInfoDropdownExpanded = false
                                }
                            )


                            DropdownMenuItem(
                                text = { Text("Move up") },
                                onClick = {
                                    viewModel.moveTimingData(i, i - 1)
                                    timingDataInfoDropdownExpanded = false
                                },
                                enabled = isMoveUpValid
                            )
                            DropdownMenuItem(
                                text = { Text("Move down") },
                                onClick = {
                                    viewModel.moveTimingData(i, i + 1)
                                    timingDataInfoDropdownExpanded = false
                                },
                                enabled = isMoveDownValid
                            )


                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    viewModel.removeTimingData(i)
                                    timingDataInfoDropdownExpanded = false
                                }
                            )
                        }
                    }


                    item {
                        Button(
                            onClick = {
                                viewModel.updateEndToCurrentPosition(i)
                                viewModel.setNewTimingData()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("<>")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.updateTimingData(
                                    i,
                                    timingData[i].startTime,
                                    timingData[i].endTime - deltaDuration
                                )
                                viewModel.setNewTimingData()
                                viewModel.playTimingDataAt(i, startFromEnd = true)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "<")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.updateTimingData(
                                    i,
                                    timingData[i].startTime,
                                    timingData[i].endTime + deltaDuration
                                )
                                viewModel.setNewTimingData()
                                viewModel.playTimingDataAt(i, startFromEnd = true)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = ">")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 6.dp))
        }

        var openAlertDialog by remember { mutableStateOf(false) }
        val currentName by viewModel.currentCustomizedSongName.collectAsState()
        var customizedSongName by remember { mutableStateOf(currentName ?: "") }

        // TODO: Is this a good way to keep states synced?
        LaunchedEffect(currentName) {
            customizedSongName = currentName ?: ""
        }

        Button(onClick = {
            openAlertDialog = true
        }) {
            Text("Save")
        }

        if (openAlertDialog) {
            // TODO: Show error
            val error by viewModel.customizedSongError.collectAsState()

            BasicAlertDialog(
                onDismissRequest = { openAlertDialog = false },
            ) {
                Column {
                    TextField(
                        value = customizedSongName,
                        onValueChange = { customizedSongName = it })
                    Button(
                        onClick = {
                            viewModel.saveNewCustomizedSong(customizedSongName)
                            if (error == null)
                                openAlertDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}