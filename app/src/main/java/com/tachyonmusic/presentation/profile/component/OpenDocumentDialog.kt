package com.tachyonmusic.presentation.profile.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun OpenDocumentDialog(show: Boolean, mimeTypeFilter: String, onResult: (Uri?) -> Unit) {
    if (!show)
        return

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = onResult
    )

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(mimeTypeFilter))
    }
}