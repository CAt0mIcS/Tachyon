package com.tachyonmusic.core.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.ms

class FileSongMetadataExtractor(
    private val contentResolver: ContentResolver,
    private val log: Logger
) : SongMetadataExtractor {
    override fun loadMetadata(
        uri: Uri,
        defaultTitle: String
    ): SongMetadataExtractor.SongMetadata? {
        val metaRetriever = MediaMetadataRetriever()
        try {
            val fd = contentResolver.openFileDescriptor(uri, "r")
            metaRetriever.setDataSource(fd?.fileDescriptor)
            fd?.close()
        } catch (e: IllegalArgumentException) {
            log.error(e.localizedMessage ?: e.stackTraceToString())
        } catch (e: SecurityException) {
            log.error(e.localizedMessage ?: e.stackTraceToString())
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

    override fun loadBitmap(uri: Uri, quality: Int): Bitmap? {
        val metaRetriever = MediaMetadataRetriever()
        try {
            val fd = contentResolver.openFileDescriptor(uri, "r")
            metaRetriever.setDataSource(fd?.fileDescriptor)
            fd?.close()
        } catch (e: IllegalArgumentException) {
            log.error(e.localizedMessage ?: e.stackTraceToString())
            return null
        } catch (e: SecurityException) {
            log.error(e.localizedMessage ?: e.stackTraceToString())
            return null
        }

        val art: ByteArray? = metaRetriever.embeddedPicture
        if (art != null) {
            val uncompressed = BitmapFactory.decodeByteArray(art, 0, art.size)
            return uncompressed
//            val stream = ByteArrayOutputStream()
//            uncompressed.compress(Bitmap.CompressFormat.JPEG, quality, stream)
//            return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        }
        return null
    }
}