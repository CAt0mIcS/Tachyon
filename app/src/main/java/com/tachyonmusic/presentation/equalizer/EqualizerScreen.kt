package com.tachyonmusic.presentation.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.presentation.NavigationItem

object EqualizerScreen : NavigationItem("equalizer") {
    @Composable
    operator fun invoke(
        viewModel: EqualizerViewModel = hiltViewModel()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Speed/Pitch")

            val state by viewModel.state.collectAsState()

            var text by remember { mutableStateOf("1.0") }
            TextField(
                value = text,
                onValueChange = {
                    text = it
                    val num = it.toFloatOrNull() ?: 1f
                    if (num > 0f)
                        viewModel.onStateChanged(num)
                })
        }
    }
}

