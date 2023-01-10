package com.tachyonmusic.presentation.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.os.Build
import androidx.annotation.RequiresApi

sealed class Permission(vararg val permissions: String) {
    // Individual permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    object ReadMediaAudio : Permission(READ_MEDIA_AUDIO)
    object ReadExternalStorage : Permission(READ_EXTERNAL_STORAGE)

    // Bundled permissions
//    object MandatoryForFeatureOne : Permission(WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)

    // Grouped permissions
//    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
//    object Storage : Permission(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)


    companion object {
        fun from(permission: String) = when (permission) {
//            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
//            WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE -> Storage
            READ_EXTERNAL_STORAGE -> ReadExternalStorage
            READ_MEDIA_AUDIO -> ReadMediaAudio
//            CAMERA -> Camera
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}