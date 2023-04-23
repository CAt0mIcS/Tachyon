package com.tachyonmusic.presentation.equalizer

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.core.domain.model.mDb
import com.tachyonmusic.presentation.NavigationItem
import com.tachyonmusic.presentation.theme.Theme

object EqualizerScreen : NavigationItem("equalizer") {
    @Composable
    operator fun invoke(
        viewModel: EqualizerViewModel = hiltViewModel()
    ) {
        val bass by viewModel.bassBoost.collectAsState()
        val virtualizer by viewModel.virtualizerStrength.collectAsState()
        val equalizer by viewModel.equalizer.collectAsState()
        val playbackParams by viewModel.playbackParameters.collectAsState()
        val reverb by viewModel.reverb.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Save")
            }

            CheckboxText(
                checked = bass != null,
                onCheckedChange = viewModel::setBassBoostEnabled,
                text = "Bass"
            )
            CheckboxText(
                checked = virtualizer != null,
                onCheckedChange = viewModel::setVirtualizerEnabled,
                text = "Virtualizer"
            )
            CheckboxText(
                checked = !equalizer.bands.isNullOrEmpty(),
                onCheckedChange = viewModel::setEqualizerEnabled,
                text = "Equalizer"
            )
            CheckboxText(
                checked = reverb != null,
                onCheckedChange = viewModel::setReverbEnabled,
                text = "Reverb"
            )

            val sliderColors = SliderDefaults.colors(
                thumbColor = Theme.colors.orange,
                activeTrackColor = Theme.colors.orange,
                inactiveTrackColor = Theme.colors.partialOrange1
            )

            if (bass != null) {
                Text(text = "Bass")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = bass!!.toFloat(),
                    onValueChange = { viewModel.setBass(it.toInt()) },
                    valueRange = 0f..1000f,
                    colors = sliderColors
                )
            }

            if (virtualizer != null) {
                Text(text = "Virtualizer Strength")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = virtualizer!!.toFloat(),
                    onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                    valueRange = 0f..1000f,
                    colors = sliderColors
                )
            }


            Text(text = "Speed")
            var speedText by remember { mutableStateOf(playbackParams.speed.toString()) }
            TextField(
                value = speedText,
                onValueChange = {
                    speedText = it
                    val num = it.toFloatOrNull() ?: return@TextField
                    if (num > 0f)
                        viewModel.setSpeed(num)
                })

            Text(text = "Pitch")
            var pitchText by remember { mutableStateOf(playbackParams.pitch.toString()) }
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
                value = playbackParams.volume,
                onValueChange = viewModel::setVolume,
                valueRange = 0f..10f,
                colors = sliderColors
            )

            if (equalizer.bands != null) {
                for (bandNumber in equalizer.bands!!.indices) {
                    val band = equalizer.bands!![bandNumber]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${band.lowerBandFrequency.inWholeHz} Hz")

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${band.centerFrequency.inWholeHz} Hz")
                            Slider(
                                modifier = Modifier.systemGestureExclusion(),
                                value = band.level.inmDb.toFloat(),
                                onValueChange = {
                                    viewModel.setBandLevel(bandNumber, it.toInt().mDb)
                                },
                                valueRange = equalizer.minBandLevel.inmDb.toFloat()..equalizer.maxBandLevel.inmDb.toFloat(),
                                colors = sliderColors
                            )
                        }

                        Text("${band.upperBandFrequency.inWholeHz} Hz")
                    }
                }
            }


            /**************************************************************************
             ********** Reverb
             *************************************************************************/
            if (reverb != null) {
                Spacer(modifier = Modifier.height(32.dp))

                Text("roomLevel")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.roomLevel.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(roomLevel = it.toInt()))
                    },
                    valueRange = -9000f..0f,
                    colors = sliderColors
                )

                Text("roomHFLevel")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.roomHFLevel.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(roomHFLevel = it.toInt()))
                    },
                    valueRange = -9000f..0f,
                    colors = sliderColors
                )

                Text("decayTime")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.decayTime.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(decayTime = it.toInt()))
                    },
                    valueRange = 100f..20000f,
                    colors = sliderColors
                )

                Text("decayHFRatio")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.decayHFRatio.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(decayHFRatio = it.toInt()))
                    },
                    valueRange = 100f..2000f,
                    colors = sliderColors
                )

                Text("reflectionsLevel")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.reflectionsLevel.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(reflectionsLevel = it.toInt()))
                    },
                    valueRange = -9000f..1000f,
                    colors = sliderColors
                )

                Text("reflectionsDelay")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.reflectionsDelay.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(reflectionsDelay = it.toInt()))
                    },
                    valueRange = 0f..3300f,
                    colors = sliderColors
                )

                Text("reverbLevel")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.reverbLevel.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(reverbLevel = it.toInt()))
                    },
                    valueRange = -9000f..2000f,
                    colors = sliderColors
                )

                Text("reverbDelay")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.reverbDelay.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(reverbDelay = it.toInt()))
                    },
                    valueRange = 0f..100f,
                    colors = sliderColors
                )

                Text("diffusion")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.diffusion.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(diffusion = it.toInt()))
                    },
                    valueRange = 0f..1000f,
                    colors = sliderColors
                )

                Text("density")
                Slider(
                    modifier = Modifier.systemGestureExclusion(),
                    value = reverb!!.density.toFloat(),
                    onValueChange = {
                        viewModel.setReverb(reverb!!.copy(density = it.toInt()))
                    },
                    valueRange = 0f..2000f,
                    colors = sliderColors
                )
            }
        }
    }
}

@Composable
fun CheckboxText(
    checked: Boolean,
    text: String,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Row(modifier = modifier) {
        Checkbox(checked, onCheckedChange, Modifier, enabled, interactionSource, colors)
        Text(text)
    }
}