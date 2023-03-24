package com.tachyonmusic.permission.domain

import kotlinx.coroutines.flow.StateFlow

interface UriPermissionRepository {
    val permissions: StateFlow<Boolean>

    fun dispatchUpdate()
}