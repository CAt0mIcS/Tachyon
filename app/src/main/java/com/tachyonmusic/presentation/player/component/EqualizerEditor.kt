package com.tachyonmusic.presentation.player.component

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.model.mDb
import com.tachyonmusic.presentation.player.EqualizerViewModel
import com.tachyonmusic.presentation.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerEditor(
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val bass by viewModel.bassBoost.collectAsState()
    val virtualizer by viewModel.virtualizerStrength.collectAsState()
    val equalizer by viewModel.equalizer.collectAsState()
    val playbackParams by viewModel.playbackParameters.collectAsState()
    val reverb by viewModel.reverb.collectAsState()

    Column(modifier = modifier) {

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

        if (bass != null) {
            Text(text = "Bass")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = bass!!.toFloat(),
                onValueChange = { viewModel.setBass(it.toInt()) },
                valueRange = 0f..1000f,
            )
        }

        if (virtualizer != null) {
            Text(text = "Virtualizer Strength")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = virtualizer!!.toFloat(),
                onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                valueRange = 0f..1000f,
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
        )

        if (equalizer.bands != null) {
            if (equalizer.presets.isNotEmpty()) {
                var isSelectingEqualizerPreset by remember { mutableStateOf(false) }

                Button(onClick = { isSelectingEqualizerPreset = !isSelectingEqualizerPreset }) {
                    Text("Equalizer Preset")
                }

                DropdownMenu(
                    modifier = Modifier.padding(Theme.padding.medium),
                    expanded = isSelectingEqualizerPreset,
                    onDismissRequest = { isSelectingEqualizerPreset = false }
                ) {
                    for (preset in equalizer.presets) {
                        DropdownMenuItem(text = { Text(preset) }, onClick = {
                            isSelectingEqualizerPreset = false
                            viewModel.setEqualizerPreset(preset)
                        })
                    }
                }

//
//                var isSelectingEqualizerPreset by remember { mutableStateOf(false) }
//
//                ExposedDropdownMenuBox(
//                    modifier = Modifier.height(200.dp),
//                    expanded = isSelectingEqualizerPreset,
//                    onExpandedChange = { isSelectingEqualizerPreset = it }
//                ) {
//                    TextField(
//                        value = equalizer.currentPreset ?: "Custom",
//                        onValueChange = {},
//                        readOnly = true,
//                        trailingIcon = {
//                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSelectingEqualizerPreset)
//                        },
//                        placeholder = {
//                            Text(text = "Reverb Preset")
//                        },
//                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
//                        modifier = Modifier.menuAnchor()
//                    )
//
//                    ExposedDropdownMenu(
//                        expanded = isSelectingEqualizerPreset,
//                        onDismissRequest = { isSelectingEqualizerPreset = false }
//                    ) {
//                        for (preset in equalizer.presets) {
//                            DropdownMenuItem(text = { Text(preset) }, onClick = {
//                                isSelectingEqualizerPreset = false
//                                viewModel.setEqualizerPreset(preset)
//                            })
//                        }
//                    }
//                }
            }

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

            var reverbPresetMenuExpanded by remember { mutableStateOf(false) }
            val selectedReverbText by viewModel.selectedReverbText.collectAsState()


            ExposedDropdownMenuBox(
                expanded = reverbPresetMenuExpanded,
                onExpandedChange = { reverbPresetMenuExpanded = it },
            ) {

                TextField(
                    value = stringResource(selectedReverbText),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reverbPresetMenuExpanded) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = reverbPresetMenuExpanded,
                    onDismissRequest = { reverbPresetMenuExpanded = false }
                ) {
                    val applyReverb = { reverb: ReverbConfig ->
                        viewModel.setReverb(reverb)
                        reverbPresetMenuExpanded = false
                    }

                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_GENERIC.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_GENERIC
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_PADDEDCELL.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_PADDEDCELL
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_ROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_ROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_BATHROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_BATHROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_LIVINGROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_LIVINGROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_STONEROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_STONEROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_AUDITORIUM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_AUDITORIUM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_CONCERTHALL.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_CONCERTHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_CAVE.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_CAVE
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_ARENA.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_ARENA
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_HANGAR.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_HANGAR
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_CARPETEDHALLWAY.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_CARPETEDHALLWAY
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_HALLWAY.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_HALLWAY
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_STONECORRIDOR.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_STONECORRIDOR
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_ALLEY.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_ALLEY
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_FOREST.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_FOREST
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_CITY.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_CITY
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_MOUNTAINS.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_MOUNTAINS
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_QUARRY.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_QUARRY
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_PLAIN.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_PLAIN
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_PARKINGLOT.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_PARKINGLOT
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_SEWERPIPE.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_SEWERPIPE
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_UNDERWATER.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_UNDERWATER
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_SMALLROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_SMALLROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_MEDIUMROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_MEDIUMROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_LARGEROOM.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_LARGEROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_MEDIUMHALL.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_MEDIUMHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_LARGEHALL.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_LARGEHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(ReverbConfig.PRESET_PLATE.toPresetStringId()) {
                        applyReverb(
                            ReverbConfig.PRESET_PLATE
                        )
                    }
                }
            }

            Text("roomLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.roomLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(roomLevel = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.ROOM_LEVEL_MIN.toFloat()..ReverbConfig.ROOM_LEVEL_MAX.toFloat(),
            )

            Text("roomHFLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.roomHFLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(roomHFLevel = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.ROOM_HF_LEVEL_MIN.toFloat()..ReverbConfig.ROOM_HF_LEVEL_MAX.toFloat(),
            )

            Text("decayTime")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.decayTime.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(decayTime = it.toInt()))
                },
                valueRange = ReverbConfig.DECAY_TIME_MIN.toFloat()..ReverbConfig.DECAY_TIME_MAX.toFloat(),
            )

            Text("decayHFRatio")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.decayHFRatio.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(decayHFRatio = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.DECAY_HF_RATIO_MIN.toFloat()..ReverbConfig.DECAY_HF_RATIO_MAX.toFloat(),
            )

            Text("reflectionsLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.reflectionsLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(reflectionsLevel = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.REFLECTIONS_LEVEL_MIN.toFloat()..ReverbConfig.REFLECTIONS_LEVEL_MAX.toFloat(),
            )

            Text("reflectionsDelay")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.reflectionsDelay.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(reflectionsDelay = it.toInt()))
                },
                valueRange = ReverbConfig.REFLECTIONS_DELAY_MIN.toFloat()..ReverbConfig.REFLECTIONS_DELAY_MAX.toFloat(),
            )

            Text("reverbLevel")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.reverbLevel.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(reverbLevel = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.REVERB_LEVEL_MIN.toFloat()..ReverbConfig.REVERB_LEVEL_MAX.toFloat(),
            )

            Text("reverbDelay")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.reverbDelay.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(reverbDelay = it.toInt()))
                },
                valueRange = ReverbConfig.REVERB_DELAY_MIN.toFloat()..ReverbConfig.REVERB_DELAY_MAX.toFloat(),
            )

            Text("diffusion")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.diffusion.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(diffusion = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.DIFFUSION_MIN.toFloat()..ReverbConfig.DIFFUSION_MAX.toFloat(),
            )

            Text("density")
            Slider(
                modifier = Modifier.systemGestureExclusion(),
                value = reverb!!.density.toFloat(),
                onValueChange = {
                    viewModel.setReverb(reverb!!.copy(density = it.toInt().toShort()))
                },
                valueRange = ReverbConfig.DENSITY_MIN.toFloat()..ReverbConfig.DENSITY_MAX.toFloat(),
            )
        }
    }
}

@Composable
private fun CheckboxText(
    checked: Boolean,
    text: String,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Row(modifier = modifier) {
        Checkbox(checked, onCheckedChange, Modifier, enabled, colors, interactionSource)
        Text(text, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun ReverbPresetDropdownMenuItem(@StringRes name: Int, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(name), color = MaterialTheme.colorScheme.onSurface) },
        onClick = onClick
    )
}
