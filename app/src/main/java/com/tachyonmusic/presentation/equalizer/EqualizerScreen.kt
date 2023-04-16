package com.tachyonmusic.presentation.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.presentation.NavigationItem
import com.tachyonmusic.presentation.theme.Theme

object EqualizerScreen : NavigationItem("equalizer") {
    @Composable
    operator fun invoke(
        viewModel: EqualizerViewModel = hiltViewModel()
    ) {
        val equalizer by viewModel.equalizerState.collectAsState()
        val enabled by viewModel.enabled.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Save")
            }

            Button(onClick = viewModel::switchEnabled) {
                Text(text = if (enabled) "Disable" else "Enable")
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

            Text(text = "Volume")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = equalizer.volume,
                onValueChange = viewModel::setVolume,
                valueRange = 0f..10f,
                colors = sliderColors
            )

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

            /**************************************************************************
             ********** Reverb
             *************************************************************************/
            Spacer(modifier = Modifier.height(32.dp))
            val reverb by viewModel.reverb.collectAsState()

            Text("roomLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.roomLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(roomLevel = it.toInt()))
                },
                valueRange = -9000f..0f,
                colors = sliderColors
            )

            Text("roomHFLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.roomHFLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(roomHFLevel = it.toInt()))
                },
                valueRange = -9000f..0f,
                colors = sliderColors
            )

            Text("decayTime")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.decayTime.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(decayTime = it.toInt()))
                },
                valueRange = 100f..20000f,
                colors = sliderColors
            )

            Text("decayHFRatio")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.decayHFRatio.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(decayHFRatio = it.toInt()))
                },
                valueRange = 100f..2000f,
                colors = sliderColors
            )

            Text("reflectionsLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.reflectionsLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(reflectionsLevel = it.toInt()))
                },
                valueRange = -9000f..1000f,
                colors = sliderColors
            )

            Text("reflectionsDelay")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.reflectionsDelay.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(reflectionsDelay = it.toInt()))
                },
                valueRange = 0f..3300f,
                colors = sliderColors
            )

            Text("reverbLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.reverbLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(reverbLevel = it.toInt()))
                },
                valueRange = -9000f..2000f,
                colors = sliderColors
            )

            Text("reverbDelay")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.reverbDelay.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(reverbDelay = it.toInt()))
                },
                valueRange = 0f..100f,
                colors = sliderColors
            )

            Text("diffusion")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.diffusion.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(diffusion = it.toInt()))
                },
                valueRange = 0f..1000f,
                colors = sliderColors
            )

            Text("density")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb.density.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb.copy(density = it.toInt()))
                },
                valueRange = 0f..2000f,
                colors = sliderColors
            )
        }
    }
}

