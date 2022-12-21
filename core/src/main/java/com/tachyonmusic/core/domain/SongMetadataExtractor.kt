package com.tachyonmusic.core.domain

import android.graphics.Bitmap
import android.net.Uri

interface SongMetadataExtractor {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val duration: Long,
        val uri: Uri
    )

    fun loadMetadata(uri: Uri): SongMetadata?
    fun loadBitmap(uri: Uri): Bitmap?
}