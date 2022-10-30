package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem
import com.tachyonmusic.presentation.player.component.cast.MediaRouter

object PlayerScreen : NavigationItem("player_screen") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        val playbackState = viewModel.playbackState.value
        val currentPosState = viewModel.currentPosition.value
        val loopState = viewModel.loopState

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MediaRouter(modifier = Modifier.size(48.dp), iconWidth = 48.dp)

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