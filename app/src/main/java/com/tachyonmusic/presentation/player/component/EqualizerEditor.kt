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
            var selectedReverbText by remember { mutableIntStateOf(R.string.reverb_generic_name) }


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

                        selectedReverbText = when (reverb) {
                            ReverbConfig.PRESET_GENERIC -> R.string.reverb_generic_name
                            ReverbConfig.PRESET_PADDEDCELL -> R.string.reverb_paddedcell_name
                            ReverbConfig.PRESET_ROOM -> R.string.reverb_room_name
                            ReverbConfig.PRESET_BATHROOM -> R.string.reverb_bathroom_name
                            ReverbConfig.PRESET_LIVINGROOM -> R.string.reverb_livingroom_name
                            ReverbConfig.PRESET_STONEROOM -> R.string.reverb_stoneroom_name
                            ReverbConfig.PRESET_AUDITORIUM -> R.string.reverb_auditorium_name
                            ReverbConfig.PRESET_CONCERTHALL -> R.string.reverb_concerthall_name
                            ReverbConfig.PRESET_CAVE -> R.string.reverb_cave_name
                            ReverbConfig.PRESET_ARENA -> R.string.reverb_arena_name
                            ReverbConfig.PRESET_HANGAR -> R.string.reverb_hangar_name
                            ReverbConfig.PRESET_CARPETEDHALLWAY -> R.string.reverb_carpetedhallway_name
                            ReverbConfig.PRESET_HALLWAY -> R.string.reverb_hallway_name
                            ReverbConfig.PRESET_STONECORRIDOR -> R.string.reverb_stonecorridor_name
                            ReverbConfig.PRESET_ALLEY -> R.string.reverb_alley_name
                            ReverbConfig.PRESET_FOREST -> R.string.reverb_forest_name
                            ReverbConfig.PRESET_CITY -> R.string.reverb_city_name
                            ReverbConfig.PRESET_MOUNTAINS -> R.string.reverb_mountains_name
                            ReverbConfig.PRESET_QUARRY -> R.string.reverb_quarry_name
                            ReverbConfig.PRESET_PLAIN -> R.string.reverb_plain_name
                            ReverbConfig.PRESET_PARKINGLOT -> R.string.reverb_parkinglot_name
                            ReverbConfig.PRESET_SEWERPIPE -> R.string.reverb_sewerpipe_name
                            ReverbConfig.PRESET_UNDERWATER -> R.string.reverb_underwater_name
                            ReverbConfig.PRESET_SMALLROOM -> R.string.reverb_smallroom_name
                            ReverbConfig.PRESET_MEDIUMROOM -> R.string.reverb_mediumroom_name
                            ReverbConfig.PRESET_LARGEROOM -> R.string.reverb_largeroom_name
                            ReverbConfig.PRESET_MEDIUMHALL -> R.string.reverb_mediumhall_name
                            ReverbConfig.PRESET_LARGEHALL -> R.string.reverb_largehall_name
                            ReverbConfig.PRESET_PLATE -> R.string.reverb_plate_name
                            else -> R.string.reverb_generic_name
                        }
                    }

                    ReverbPresetDropdownMenuItem(R.string.reverb_generic_name) {
                        applyReverb(
                            ReverbConfig.PRESET_GENERIC
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_paddedcell_name) {
                        applyReverb(
                            ReverbConfig.PRESET_PADDEDCELL
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_room_name) {
                        applyReverb(
                            ReverbConfig.PRESET_ROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_bathroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_BATHROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_livingroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_LIVINGROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_stoneroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_STONEROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_auditorium_name) {
                        applyReverb(
                            ReverbConfig.PRESET_AUDITORIUM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_concerthall_name) {
                        applyReverb(
                            ReverbConfig.PRESET_CONCERTHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_cave_name) {
                        applyReverb(
                            ReverbConfig.PRESET_CAVE
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_arena_name) {
                        applyReverb(
                            ReverbConfig.PRESET_ARENA
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_hangar_name) {
                        applyReverb(
                            ReverbConfig.PRESET_HANGAR
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_carpetedhallway_name) {
                        applyReverb(
                            ReverbConfig.PRESET_CARPETEDHALLWAY
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_hallway_name) {
                        applyReverb(
                            ReverbConfig.PRESET_HALLWAY
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_stonecorridor_name) {
                        applyReverb(
                            ReverbConfig.PRESET_STONECORRIDOR
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_alley_name) {
                        applyReverb(
                            ReverbConfig.PRESET_ALLEY
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_forest_name) {
                        applyReverb(
                            ReverbConfig.PRESET_FOREST
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_city_name) {
                        applyReverb(
                            ReverbConfig.PRESET_CITY
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_mountains_name) {
                        applyReverb(
                            ReverbConfig.PRESET_MOUNTAINS
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_quarry_name) {
                        applyReverb(
                            ReverbConfig.PRESET_QUARRY
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_plain_name) {
                        applyReverb(
                            ReverbConfig.PRESET_PLAIN
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_parkinglot_name) {
                        applyReverb(
                            ReverbConfig.PRESET_PARKINGLOT
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_sewerpipe_name) {
                        applyReverb(
                            ReverbConfig.PRESET_SEWERPIPE
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_underwater_name) {
                        applyReverb(
                            ReverbConfig.PRESET_UNDERWATER
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_smallroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_SMALLROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_mediumroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_MEDIUMROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_largeroom_name) {
                        applyReverb(
                            ReverbConfig.PRESET_LARGEROOM
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_mediumhall_name) {
                        applyReverb(
                            ReverbConfig.PRESET_MEDIUMHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_largehall_name) {
                        applyReverb(
                            ReverbConfig.PRESET_LARGEHALL
                        )
                    }
                    ReverbPresetDropdownMenuItem(R.string.reverb_plate_name) {
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
        Text(text)
    }
}

@Composable
private fun ReverbPresetDropdownMenuItem(@StringRes name: Int, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(name), color = MaterialTheme.colorScheme.onSurface) },
        onClick = onClick
    )
}
