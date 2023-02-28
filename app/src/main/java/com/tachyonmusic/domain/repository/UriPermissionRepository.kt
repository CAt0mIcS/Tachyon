package com.tachyonmusic.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface UriPermissionRepository {
    val permissions: StateFlow<Boolean>

    fun dispatchUpdate()
}