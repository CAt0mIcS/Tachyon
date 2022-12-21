package com.tachyonmusic.core.domain

import android.graphics.Bitmap
import java.net.URI

interface SongMetadataExtractor {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val duration: Long,
        val uri: URI
    )

    fun loadMetadata(uri: URI): SongMetadata?
    fun loadBitmap(uri: URI): Bitmap?
}