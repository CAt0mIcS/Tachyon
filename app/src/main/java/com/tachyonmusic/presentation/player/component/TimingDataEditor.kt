package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.core_components.ErrorDialog
import com.tachyonmusic.presentation.player.TimingDataEditorViewModel
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.asString
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
        IconButton(
            modifier = Modifier.padding(Theme.padding.small),
            onClick = { viewModel.addNewTimingData(timingData.size) }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_circle),
                contentDescription = "Add new customizedSong time point"
            )
        }

        IconButton(
            modifier = Modifier.padding(Theme.padding.small),
            onClick = { viewModel.removeTimingData(timingData.size - 1) }) {
            Icon(
                painterResource(R.drawable.ic_rewind),
                contentDescription = "Remove customizedSong time point"
            )
        }

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

                LazyRow(modifier = Modifier.fillMaxWidth()) {

                    val deltaDuration = viewModel.settings.value.audioUpdateInterval

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
            }


            Spacer(modifier = Modifier.padding(top = 6.dp))
        }

        var openAlertDialog by remember { mutableStateOf(false) }
        var customizedSongName by remember { mutableStateOf("") }

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