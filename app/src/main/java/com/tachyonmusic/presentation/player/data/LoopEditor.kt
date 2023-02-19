package com.tachyonmusic.presentation.player.data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RangeSlider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.presentation.player.LoopEditorViewModel
import com.tachyonmusic.presentation.player.PlayerViewModel
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoopEditor(
    modifier: Modifier = Modifier,
    viewModel: LoopEditorViewModel = hiltViewModel()
) {
    val timingData = viewModel.timingData

    Column(modifier = modifier.padding(start = Theme.padding.extraSmall)) {
        IconButton(
            modifier = modifier.padding(Theme.padding.small),
            onClick = { viewModel.addNewTimingData(timingData.size) }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_circle),
                contentDescription = "Add new loop time point"
            )
        }

        IconButton(
            modifier = Modifier.padding(Theme.padding.small),
            onClick = { viewModel.removeTimingData(timingData.size - 1) }) {
            Icon(
                painterResource(R.drawable.ic_rewind_10),
                contentDescription = "Remove loop time point"
            )
        }

        val duration by viewModel.duration.collectAsState()
        val currentIndex by viewModel.currentIndex.collectAsState()

        for (i in timingData.indices) {

            RangeSlider(
                value = timingData[i].startTime.inWholeMilliseconds.toFloat()..timingData[i].endTime.inWholeMilliseconds.toFloat(),
                onValueChange = {
                    viewModel.updateTimingData(i, it.start.ms, it.endInclusive.ms)
                },
                onValueChangeFinished = viewModel::setNewTimingData,
                valueRange = 0f..duration.inWholeMilliseconds.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = if (i == currentIndex) Theme.colors.orange else Theme.colors.contrastLow,
                    activeTrackColor = Theme.colors.orange,
                    inactiveTrackColor = Theme.colors.partialOrange1
                )
            )

            Spacer(modifier = Modifier.padding(top = 6.dp))
        }

        var openAlertDialog by remember { mutableStateOf(false) }
        var loopName by remember { mutableStateOf("") }

        Button(onClick = {
            openAlertDialog = true
        }) {
            Text("Save")
        }

        if (openAlertDialog) {
            // TODO: Show error
            val error by viewModel.loopError.collectAsState()

            AlertDialog(
                onDismissRequest = { openAlertDialog = false },
                text = {
                    TextField(value = loopName, onValueChange = { loopName = it })
                },
                buttons = {
                    Button(
                        onClick = {
                            viewModel.saveNewLoop(loopName)
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