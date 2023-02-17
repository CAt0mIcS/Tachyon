package com.tachyonmusic.core.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.util.File
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.nameWithoutExtension

class FileSongMetadataExtractor : SongMetadataExtractor {
    override fun loadMetadata(uri: Uri): SongMetadataExtractor.SongMetadata? {
        if (!uri.isAbsolute || !File(uri.path!!).exists())
            return null

        val path = File(uri.path!!)

        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(path.absolutePath)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: ${path.absolutePath}, ${e.localizedMessage}")
        }

        return SongMetadataExtractor.SongMetadata(
            title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: path.nameWithoutExtension,
            artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist",
            duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()?.ms ?: 0.ms,
            uri = uri
        )
    }

    override fun loadBitmap(uri: Uri): Bitmap? {
        if (!uri.isAbsolute || !File(uri.path!!).exists())
            return null

        val path = File(uri.path!!)

        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(path.absolutePath)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: ${path.absolutePath}")
        }

        val art: ByteArray? = metaRetriever.embeddedPicture
        if (art != null) {
            return BitmapFactory.decodeByteArray(art, 0, art.size)
        }
        return null
    }
}