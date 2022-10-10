package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem
import com.tachyonmusic.presentation.player.component.MultiPositionSeekBar

object PlayerScreen : NavigationItem("player_screen") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
//            MediaRouter(modifier = Modifier.size(16.dp), iconWidth = 16.dp)

            Text(text = viewModel.playbackState.value.title)
            Text(text = viewModel.playbackState.value.artist)
            Text(text = viewModel.playbackState.value.durationString)
            Text(text = viewModel.currentPosition.value)

//            MultiPositionSeekBar(
//                min = 0,
//                max = viewModel.playbackState.value.duration,
//                positions = listOf(0, viewModel.playbackState.value.duration),
//                divisionFactor = 100
//            )
        }

    }
}