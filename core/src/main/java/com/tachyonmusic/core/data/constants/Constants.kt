package com.tachyonmusic.core.data.constants

import android.os.Environment


object Constants {
    // Stored for performance reasons
    val EXTERNAL_STORAGE_DIRECTORY: String =
        Environment.getExternalStorageDirectory().absolutePath
}
