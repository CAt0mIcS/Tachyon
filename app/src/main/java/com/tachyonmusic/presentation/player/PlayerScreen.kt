package com.tachyonmusic.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem

object PlayerScreen : NavigationItem("player_screen") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlayerViewModel = hiltViewModel()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = viewModel.playbackState.value.title)
            Text(text = viewModel.playbackState.value.artist)
            Text(text = viewModel.playbackState.value.duration)
            Text(text = viewModel.currentPosition.value)
        }

    }
}