package com.tachyonmusic.core.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.util.ms

class FileSongMetadataExtractor : SongMetadataExtractor {
    override fun loadMetadata(
        contentResolver: ContentResolver,
        uri: Uri,
        defaultTitle: String
    ): SongMetadataExtractor.SongMetadata? {
        val metaRetriever = MediaMetadataRetriever()
        try {
            val fd = contentResolver.openFileDescriptor(uri, "r")
            metaRetriever.setDataSource(fd?.fileDescriptor)
            fd?.close()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: $uri, ${e.localizedMessage}")
        } catch (_: SecurityException) {

        }

        return SongMetadataExtractor.SongMetadata(
            title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: defaultTitle,
            artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist",
            duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()?.ms ?: 0.ms
        )
    }

    override fun loadBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        val metaRetriever = MediaMetadataRetriever()
        try {
            val fd = contentResolver.openFileDescriptor(uri, "r")
            metaRetriever.setDataSource(fd?.fileDescriptor)
            fd?.close()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: $uri")
        } catch (_: SecurityException) {

        }

        val art: ByteArray? = metaRetriever.embeddedPicture
        if (art != null) {
            return BitmapFactory.decodeByteArray(art, 0, art.size)
        }
        return null
    }
}