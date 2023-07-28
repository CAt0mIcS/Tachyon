package com.tachyonmusic.playback_layers.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UriPermissionRepositoryImpl(
    private val context: Context
) : UriPermissionRepository {
    private val _permissions = MutableStateFlow(false)
    override val permissions = _permissions.asStateFlow()

    override fun addPermissionUri(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        dispatchUpdate()
    }

    override fun hasPermission(uri: Uri) =
        context.checkUriPermission(
            uri,
            Binder.getCallingPid(),
            Binder.getCallingUid(),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


    override fun dispatchUpdate() {
        _permissions.update { !permissions.value }
    }
}