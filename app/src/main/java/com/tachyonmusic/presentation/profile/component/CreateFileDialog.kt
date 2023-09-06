package com.tachyonmusic.presentation.profile.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun CreateFileDialog(show: Boolean, mimeType: String, name: String, onResult: (Uri?) -> Unit) {
    if (!show)
        return

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType),
        onResult = onResult
    )

    LaunchedEffect(Unit) {
        launcher.launch(name)
    }
}