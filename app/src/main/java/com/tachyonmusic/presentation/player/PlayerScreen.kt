package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.RangeSlider
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        val playbackState = viewModel.playbackState.value
        val currentPosState = viewModel.currentPosition.value
        val loopState = viewModel.loopState.value

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
//            MediaRouter(modifier = Modifier.size(16.dp), iconWidth = 16.dp)

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

            RangeSlider(
                value = loopState.startTime.toFloat()..loopState.endTime.toFloat(),
                onValueChange = {
                    viewModel.onLoopStateChanged(
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