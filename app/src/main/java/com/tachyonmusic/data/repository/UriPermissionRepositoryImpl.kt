package com.tachyonmusic.data.repository

import com.tachyonmusic.domain.repository.UriPermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UriPermissionRepositoryImpl : UriPermissionRepository {
    private val _permissions = MutableStateFlow(false)
    override val permissions = _permissions.asStateFlow()

    override fun dispatchUpdate() {
        _permissions.update { !permissions.value }
    }
}