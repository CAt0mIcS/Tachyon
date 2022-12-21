package com.tachyonmusic.util

import android.graphics.Bitmap
import com.tachyonmusic.core.domain.SongMetadataExtractor
import java.net.URI

internal class TestSongMetadataExtractor : SongMetadataExtractor {
    override fun loadMetadata(uri: URI): SongMetadataExtractor.SongMetadata {
        return SongMetadataExtractor.SongMetadata(
            "Title",
            "Artist",
            duration = 10000L,
            uri = uri
        )
    }

    override fun loadBitmap(uri: URI): Bitmap? {
        return null
    }
}