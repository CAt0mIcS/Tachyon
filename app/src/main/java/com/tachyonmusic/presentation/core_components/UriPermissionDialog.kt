package com.tachyonmusic.presentation.core_components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun UriPermissionDialog(show: Boolean, onPermission: (Uri?) -> Unit) {
    if(!show)
        return

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onPermission
    )

    LaunchedEffect(Unit) {
        launcher.launch(null)
    }
}