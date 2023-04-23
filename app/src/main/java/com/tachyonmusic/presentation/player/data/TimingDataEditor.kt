package com.tachyonmusic.presentation.player.data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.player.TimingDataEditorViewModel
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.util.ms

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimingDataEditor(
    modifier: Modifier = Modifier,
    viewModel: TimingDataEditorViewModel = hiltViewModel()
) {
    val timingData = viewModel.timingData

    Column(modifier = modifier.padding(start = Theme.padding.extraSmall)) {
        IconButton(
            modifier = modifier.padding(Theme.padding.small),
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

            RangeSlider(
                modifier = Modifier.systemGestureExclusion(),
                value = timingData[i].startTime.inWholeMilliseconds.toFloat()..timingData[i].endTime.inWholeMilliseconds.toFloat(),
                onValueChange = {
                    viewModel.updateTimingData(i, it.start.ms, it.endInclusive.ms)
                },
                onValueChangeFinished = viewModel::setNewTimingData,
                valueRange = 0f..duration.inWholeMilliseconds.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = if (i == viewModel.currentIndex) Theme.colors.orange else Theme.colors.contrastLow,
                    activeTrackColor = Theme.colors.orange,
                    inactiveTrackColor = Theme.colors.partialOrange1
                )
            )

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

            AlertDialog(
                onDismissRequest = { openAlertDialog = false },
                text = {
                    TextField(value = customizedSongName, onValueChange = { customizedSongName = it })
                },
                buttons = {
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
            )
        }
    }
}