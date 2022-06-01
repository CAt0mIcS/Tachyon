package com.daton.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File

class SongMetadata(path: File) {

    val title: String
    val artist: String
    var albumArt: Bitmap? = null
        private set
    val duration: Long

    init {
        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(path.absolutePath)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            TODO("Implement error handling")
        }

        title =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: path.nameWithoutExtension

        artist =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"

        val art: ByteArray? = metaRetriever.embeddedPicture
        if (art != null) {
            albumArt = BitmapFactory.decodeByteArray(art, 0, art.size)
        }

        duration =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toLong()
    }

}