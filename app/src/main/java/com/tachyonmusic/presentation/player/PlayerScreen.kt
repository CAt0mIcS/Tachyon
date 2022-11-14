package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem
import kotlinx.coroutines.delay

object PlayerScreen : NavigationItem("player_screen") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        var currentPosition by remember { mutableStateOf(0L) }
        val isPlaying by viewModel.isPlaying

        var loopName by remember { mutableStateOf("") }
        val playbackState by viewModel.playbackState

        var isSeeking by remember { mutableStateOf(false) }

        val loopState = viewModel.loopState

        DisposableEffect(Unit) {
            viewModel.registerPlayerListeners()
            onDispose {
                viewModel.unregisterPlayerListeners()
            }
        }

        if (isPlaying) {
            LaunchedEffect(Unit) {
                while (true) {
                    if (!isSeeking)
                        currentPosition = viewModel.currentPosition
                    delay(viewModel.audioUpdateInterval)
                }
            }
        }


        Column(
            modifier = Modifier.fillMaxSize()
        ) {
//            MediaRouter(modifier = Modifier.size(48.dp), iconWidth = 48.dp)

            Text(text = playbackState.title)
            Text(text = playbackState.artist)
            Text(text = playbackState.durationString)
            Text(text = viewModel.getTextForPosition(currentPosition))

            Slider(
                value = currentPosition.toFloat(),
                valueRange = 0f..playbackState.duration.toFloat(),
                onValueChangeFinished = {
                    viewModel.onSeekTo(currentPosition)
                    isSeeking = false
                },
                onValueChange = {
                    isSeeking = true
                    currentPosition = it.toLong()
                }
            )

            TextField(value = loopName, onValueChange = { loopName = it })

            Button(onClick = { viewModel.onSaveLoop(loopName) }) {
                Text("Save Loop")
            }

            Button(onClick = { viewModel.onAddTimingData() }) {
                Text(text = "Add Loop Time")
            }

            for (i in loopState.indices) {
                RangeSlider(
                    value = loopState[i].startTime.toFloat()..loopState[i].endTime.toFloat(),
                    onValueChange = {
                        viewModel.onTimingDataValuesChanged(
                            i,
                            it.start.toLong(),
                            it.endInclusive.toLong()
                        )
                    },
                    onValueChangeFinished = { viewModel.onSetUpdatedTimingData() },
                    valueRange = 0f..playbackState.duration.toFloat()
                )
            }
        }

    }
}