package com.tachyonmusic.playback_layers.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * This use-case uses the slow [DocumentFile] API to check if we can access the file behind the URI
 */
class IsUriAccessible(
    @ApplicationContext
    private val context: Context
) {
    operator fun invoke(uri: Uri?): Boolean {
        if (uri == null || uri.path == null)
            return false

        return DocumentFile.fromTreeUri(context, uri)?.canRead() == true
    }
}