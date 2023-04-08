package com.tachyonmusic.permission.domain

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface UriPermissionRepository {
    val permissions: StateFlow<Boolean>

    fun addPermissionUri(uri: Uri)
    fun hasPermission(uri: Uri): Boolean

    fun dispatchUpdate()
}