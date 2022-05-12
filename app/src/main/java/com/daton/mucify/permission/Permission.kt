package com.daton.mucify.permission

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
    // Individual permissions
    object ReadStorage : Permission(READ_EXTERNAL_STORAGE)

    // Bundled permissions
//    object MandatoryForFeatureOne : Permission(WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)

    // Grouped permissions
//    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
//    object Storage : Permission(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)


    companion object {
        fun from(permission: String) = when (permission) {
//            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
//            WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE -> Storage
            READ_EXTERNAL_STORAGE -> ReadStorage
//            CAMERA -> Camera
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}