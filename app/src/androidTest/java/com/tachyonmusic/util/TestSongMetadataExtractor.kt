package com.tachyonmusic.util

import android.graphics.Bitmap
import android.net.Uri
import com.tachyonmusic.core.domain.SongMetadataExtractor

internal class TestSongMetadataExtractor : SongMetadataExtractor {
    override fun loadMetadata(uri: Uri): SongMetadataExtractor.SongMetadata {
        return SongMetadataExtractor.SongMetadata(
            "Title",
            "Artist",
            duration = 10000L,
            uri = uri
        )
    }

    override fun loadBitmap(uri: Uri): Bitmap? {
        return null
    }
}