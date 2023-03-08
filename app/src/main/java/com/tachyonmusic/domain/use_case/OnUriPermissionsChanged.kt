package com.tachyonmusic.domain.use_case

import com.tachyonmusic.permission.domain.UriPermissionRepository

// TODO: Optimize usages and clean up
class OnUriPermissionsChanged(private val uriPermissionRepository: com.tachyonmusic.permission.domain.UriPermissionRepository) {
    operator fun invoke() = uriPermissionRepository.permissions
}