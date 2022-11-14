package com.tachyonmusic.presentation.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem

object PlayerScreen : NavigationItem("player_screen") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        val playbackState by viewModel.playbackState
        val currentPosState by viewModel.currentPosition
        val loopState = viewModel.loopState

        var loopName by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
//            MediaRouter(modifier = Modifier.size(48.dp), iconWidth = 48.dp)

            Text(text = playbackState.title)
            Text(text = playbackState.artist)
            Text(text = playbackState.durationString)
            Text(text = currentPosState.posStr)

            Slider(
                value = currentPosState.pos.toFloat(),
                valueRange = 0f..playbackState.duration.toFloat(),
                onValueChangeFinished = { viewModel.onPositionChangeFinished() },
                onValueChange = { viewModel.onPositionChange(it.toLong()) }
            )

            TextField(value = loopName, onValueChange = { loopName = it })

            Button(onClick = { viewModel.onSaveLoop(loopName) }) {
                Text("Save Loop")
            }

            Button(onClick = { viewModel.onAddNewTimingData() }) {
                Text(text = "Add Loop Time")
            }

            for (i in loopState.indices) {
                RangeSlider(
                    value = loopState[i].startTime.toFloat()..loopState[i].endTime.toFloat(),
                    onValueChange = {
                        viewModel.onLoopStateChanged(
                            i,
                            it.start.toLong(),
                            it.endInclusive.toLong()
                        )
                    },
                    onValueChangeFinished = { viewModel.onLoopStateChangeFinished() },
                    valueRange = 0f..playbackState.duration.toFloat()
                )
            }
        }
    }
}