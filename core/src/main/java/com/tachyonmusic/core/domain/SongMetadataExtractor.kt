package com.tachyonmusic.core.domain

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.tachyonmusic.util.Duration

interface SongMetadataExtractor {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val duration: Duration
    )

    fun loadMetadata(contentResolver: ContentResolver, uri: Uri, defaultTitle: String): SongMetadata?
    fun loadBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap?
}