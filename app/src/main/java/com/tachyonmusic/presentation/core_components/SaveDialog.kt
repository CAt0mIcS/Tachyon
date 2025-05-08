package com.tachyonmusic.presentation.core_components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    headlineText: String,
    textFieldContent: @Composable () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmButtonEnabled: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = Theme.shapes.medium,
            modifier = Modifier.fillMaxWidth(.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.medium)
            ) {
                Text(text = headlineText, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                textFieldContent()

                val animateStateButtonColor = animateColorAsState(
                    targetValue = if (confirmButtonEnabled)
                        ButtonDefaults.buttonColors().containerColor
                    else
                        ButtonDefaults.buttonColors().disabledContainerColor,
                    animationSpec = tween(200, 0)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onConfirm,
                        enabled = confirmButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animateStateButtonColor.value,
                            disabledContainerColor = animateStateButtonColor.value,
                        )
                    ) {
                        Text("Save")
                    }

                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}