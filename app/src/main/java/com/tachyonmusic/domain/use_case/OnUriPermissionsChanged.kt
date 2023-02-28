package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.repository.UriPermissionRepository

// TODO: Optimize usages and clean up
class OnUriPermissionsChanged(private val uriPermissionRepository: UriPermissionRepository) {
    operator fun invoke() = uriPermissionRepository.permissions
}