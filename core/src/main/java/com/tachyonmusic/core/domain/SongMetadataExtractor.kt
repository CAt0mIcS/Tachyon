package com.tachyonmusic.core.domain

import android.graphics.Bitmap
import android.net.Uri
import com.tachyonmusic.util.Duration

interface SongMetadataExtractor {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val duration: Duration
    )

    fun loadMetadata(uri: Uri, defaultTitle: String): SongMetadata?
    fun loadBitmap(uri: Uri): Bitmap?
}