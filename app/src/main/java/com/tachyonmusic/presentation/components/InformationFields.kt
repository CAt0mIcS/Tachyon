package com.tachyonmusic.presentation.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable

@Composable
fun UserInformation(
    buttonText: String,
    onButtonClicked: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    email: String = "",
    password: String = ""
) {
    TextField(value = email, onValueChange = onEmailChanged)
    TextField(value = password, onValueChange = onPasswordChanged)
    Button(onClick = onButtonClicked) {
        Text(text = buttonText)
    }
}