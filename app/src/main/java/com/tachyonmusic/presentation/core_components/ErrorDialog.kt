package com.tachyonmusic.presentation.core_components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.core_components.model.DialogAction
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun ErrorDialog(
    title: String,
    subtitle: String,
    onDismissRequest: (DialogAction) -> Boolean = { true },
    buttons: List<DialogAction> = listOf(DialogAction.CLOSE),
    properties: DialogProperties = DialogProperties()
) {

    var surfaceHeight by remember { mutableStateOf(266.dp) }
    val density = LocalDensity.current
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog)
        Dialog(
            onDismissRequest = {
                if (onDismissRequest(DialogAction.CLOSE))
                    showDialog = false
            },
            properties
        ) {
            Surface(
                shape = Theme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 266.dp)
                    .padding(horizontal = Theme.padding.medium)
                    .onSizeChanged {
                        surfaceHeight = with(density) { it.height.toDp() }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error)
                            .height(.35f * surfaceHeight)
                            .fillMaxWidth()
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error Icon",
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onError)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(
                                start = Theme.padding.large,
                                end = Theme.padding.large,
                                top = Theme.padding.large
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(bottom = Theme.padding.small)
                        )
                        Text(
                            subtitle,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(modifier = Modifier.padding(vertical = Theme.padding.medium)) {
                        val buttonColors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )

                        for (button in buttons) {
                            when (button) {
                                DialogAction.CLOSE -> {
                                    Button(
                                        colors = buttonColors,
                                        onClick = {
                                            if (onDismissRequest(DialogAction.CLOSE))
                                                showDialog = false
                                        })
                                    {
                                        Text(stringResource(R.string.close))
                                    }
                                }

                                DialogAction.RELOAD -> {
                                    Button(
                                        colors = buttonColors,
                                        onClick = {
                                            if (onDismissRequest(DialogAction.RELOAD))
                                                showDialog = false
                                        })
                                    {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}