package com.tachyonmusic.core.domain

import android.graphics.Bitmap
import android.net.Uri
import com.tachyonmusic.util.Duration

interface SongMetadataExtractor {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val duration: Duration,
        val album: String?
    )

    fun loadMetadata(uri: Uri, defaultTitle: String): SongMetadata?

    /**
     * Uses JPEG compression
     * @param quality the lower the [quality], the more compressed the image will be
     *  (accepted range: 0..100)
     */
    fun loadBitmap(uri: Uri, quality: Int = 100): Bitmap?
}