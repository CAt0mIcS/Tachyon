package com.tachyonmusic.core.data.constants

import android.os.Environment
import com.tachyonmusic.core.R
import com.tachyonmusic.core.data.ResourceArtwork


object Constants {
    // Stored for performance reasons
    val EXTERNAL_STORAGE_DIRECTORY: String =
        Environment.getExternalStorageDirectory().absolutePath
}
