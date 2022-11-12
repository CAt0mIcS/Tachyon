package com.tachyonmusic.core.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File

class SongMetadata(path: File) {

    val title: String
    val artist: String
    val duration: Long

    init {
        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(path.absolutePath)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling: ${path.absolutePath}")
        }

        title =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: path.nameWithoutExtension

        artist =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"

        duration =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toLong()
    }

    companion object {
        fun loadBitmap(path: File): Bitmap? {
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

}