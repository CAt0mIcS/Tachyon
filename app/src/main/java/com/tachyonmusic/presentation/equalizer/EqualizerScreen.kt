package com.tachyonmusic.presentation.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.presentation.NavigationItem
import com.tachyonmusic.presentation.theme.Theme

object EqualizerScreen : NavigationItem("equalizer") {
    @Composable
    operator fun invoke(
        viewModel: EqualizerViewModel = hiltViewModel()
    ) {
        val equalizer by viewModel.equalizerState.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Save")
            }

            Button(onClick = viewModel::switchEnabled) {
                Text(text = if (equalizer.enabled) "Disable" else "Enable")
            }

            val sliderColors = SliderDefaults.colors(
                thumbColor = Theme.colors.orange,
                activeTrackColor = Theme.colors.orange,
                inactiveTrackColor = Theme.colors.partialOrange1
            )

            Text(text = "Bass")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = equalizer.bass.toFloat(),
                onValueChange = { viewModel.setBass(it.toInt()) },
                valueRange = 0f..1000f,
                colors = sliderColors
            )

            Text(text = "Virtualizer Strength")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = equalizer.virtualizerStrength.toFloat(),
                onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                valueRange = 0f..1000f,
                colors = sliderColors
            )

            Text(text = "Speed")
            var speedText by remember { mutableStateOf(equalizer.speed.toString()) }
            TextField(
                value = speedText,
                onValueChange = {
                    speedText = it
                    val num = it.toFloatOrNull() ?: return@TextField
                    if (num > 0f)
                        viewModel.setSpeed(num)
                })

            Text(text = "Pitch")
            var pitchText by remember { mutableStateOf(equalizer.pitch.toString()) }
            TextField(
                value = pitchText,
                onValueChange = {
                    pitchText = it
                    val num = it.toFloatOrNull() ?: return@TextField
                    if (num > 0f)
                        viewModel.setPitch(num)
                })

            val bandLevels by viewModel.bandLevels.collectAsState()
            for (bandNumber in 0 until equalizer.numBands) {
                val level = bandLevels[bandNumber]
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = level.toFloat(),
                    onValueChange = {
                        viewModel.setBandLevel(bandNumber, it.toInt())
                    },
                    valueRange = equalizer.minBandLevel.toFloat()..equalizer.maxBandLevel.toFloat(),
                    colors = sliderColors
                )
            }
        }
    }
}

